package tw.bk.ai.service.llm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tw.bk.ai.config.AppProperties;
import tw.bk.ai.dto.chat.MessageSendReq;
import tw.bk.ai.entity.Message;
import tw.bk.ai.exception.BizException;
import tw.bk.ai.exception.ErrorCode;
import tw.bk.ai.result.Result;
import tw.bk.ai.service.chat.MessageService;
import tw.bk.ai.service.ratelimit.RateLimitService;
import tw.bk.ai.service.usage.UsageLogService;
import tw.bk.ai.vo.chat.StreamEventVo;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import reactor.core.Disposable;

/**
 * LLM 服務
 * 處理與 LLM 的互動，包含串流回覆
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmService {

    private final LlmClient llmClient;
    private final PromptBuilder promptBuilder;
    private final MessageService messageService;
    private final AppProperties appProperties;
    private final RateLimitService rateLimitService;
    private final UsageLogService usageLogService;
    @Qualifier("llmTaskExecutor")
    private final TaskExecutor llmTaskExecutor;

    /**
 * 串流生成回覆
     */
    public SseEmitter streamChat(Long userId, Long chatId, MessageSendReq req) {
        // 建立 SSE Emitter（超時時間 5 分鐘）
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L);

        AtomicReference<Disposable> subscriptionRef = new AtomicReference<>();

        llmTaskExecutor.execute(() -> {
            StringBuilder fullResponse = new StringBuilder();
            AtomicReference<int[]> usageRef = new AtomicReference<>();

            try {
                rateLimitService.check(userId);

                // 保存使用者訊息
                messageService.saveUserMessage(userId, chatId, req.getContent());

                // 取得對話歷史
                List<Message> messages = messageService.getMessages(chatId);

                // 建構 prompt
                List<Map<String, String>> prompt = promptBuilder.build(messages);

                // 取得參數
                String model = req.getModel() != null ? req.getModel() : appProperties.getLlm().getModel();
                double temperature = req.getTemperature() != null ? req.getTemperature()
                        : appProperties.getLlm().getTemperature();
                int maxTokens = req.getMaxTokens() != null ? req.getMaxTokens() : appProperties.getLlm().getMaxTokens();

                // 串流生成
                Disposable subscription = llmClient.streamChat(prompt, model, temperature, maxTokens)
                        .doOnNext(event -> {
                            try {
                                if (event.getDelta() != null && !event.getDelta().isEmpty()) {
                                    fullResponse.append(event.getDelta());
                                    sendEvent(emitter, "delta", StreamEventVo.delta(event.getDelta()));
                                }
                                if (event.getInputTokens() != null && event.getOutputTokens() != null) {
                                    usageRef.set(new int[] { event.getInputTokens(), event.getOutputTokens() });
                                }
                            } catch (IOException e) {
                                log.warn("Failed to send SSE delta: {}", e.getMessage());
                            }
                        })
                        .doOnComplete(() -> {
                            try {
                                int inputTokens = 0;
                                int outputTokens = 0;
                                int[] usage = usageRef.get();
                                if (usage != null && usage.length == 2) {
                                    inputTokens = usage[0];
                                    outputTokens = usage[1];
                                }

                                // 保存 AI 回覆
                                Message assistantMessage = messageService.saveAssistantMessage(
                                        chatId,
                                        fullResponse.toString(),
                                        llmClient.getProviderName(),
                                        model,
                                        inputTokens,
                                        outputTokens);

                                usageLogService.logUsage(
                                        userId,
                                        chatId,
                                        assistantMessage.getId(),
                                        llmClient.getProviderName(),
                                        model,
                                        inputTokens,
                                        outputTokens);

                                // 發送完成事件
                                sendEvent(emitter, "done", StreamEventVo.done(inputTokens, outputTokens));
                                emitter.complete();

                                log.info("Stream completed for chat: {}", chatId);
                            } catch (IOException e) {
                                log.error("Failed to complete SSE stream: {}", e.getMessage());
                            }
                        })
                        .doOnError(error -> {
                            handleStreamError(emitter, error, chatId);
                        })
                        .subscribe();
                subscriptionRef.set(subscription);

            } catch (Exception e) {
                handleStreamError(emitter, e, chatId);
            }
        });

        // 設定超時和錯誤處理
        emitter.onTimeout(() -> {
            log.warn("SSE timeout for chat: {}", chatId);
            disposeSubscription(subscriptionRef);
            emitter.complete();
        });

        emitter.onError(e -> {
            log.error("SSE error for chat: {}", chatId, e);
            disposeSubscription(subscriptionRef);
        });

        emitter.onCompletion(() -> disposeSubscription(subscriptionRef));

        return emitter;
    }

    private void sendEvent(SseEmitter emitter, String eventName, StreamEventVo data) throws IOException {
        emitter.send(SseEmitter.event()
                .name(eventName)
                .data(data));
    }

    private void handleStreamError(SseEmitter emitter, Throwable error, Long chatId) {
        log.error("Stream error for chat: {}", chatId, error);
        try {
            Result<Object> errorResult;
            if (error instanceof BizException biz) {
                errorResult = Result.fail(biz.getCode(), biz.getMessage(), biz.getDetails());
            } else {
                errorResult = Result.fail(
                        ErrorCode.LLM_STREAM_ERROR.getCode(),
                        error.getMessage());
            }
            sendEvent(emitter, "error", StreamEventVo.error(errorResult));
            emitter.complete();
        } catch (IOException e) {
            log.error("Failed to send error event: {}", e.getMessage());
            emitter.completeWithError(error);
        }
    }

    private void disposeSubscription(AtomicReference<Disposable> subscriptionRef) {
        Disposable disposable = subscriptionRef.get();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}
