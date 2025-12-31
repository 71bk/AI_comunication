package tw.bk.ai.service.llm.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import tw.bk.ai.service.llm.LlmClient;
import tw.bk.ai.service.llm.LlmStreamEvent;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * OpenAI LLM 客戶端實作（Mock 版本）
 * 
 * TODO: 實作真正的 OpenAI API 呼叫
 * 目前為模擬實作，用於測試 SSE 串流功能
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.llm.provider", havingValue = "openai", matchIfMissing = true)
public class OpenAiLlmClient implements LlmClient {

    @Value("${app.llm.api-key:}")
    private String apiKey;

    @Value("${app.llm.model:gpt-4o-mini}")
    private String defaultModel;

    @Override
    public Flux<LlmStreamEvent> streamChat(List<Map<String, String>> messages, String model, double temperature,
            int maxTokens) {
        log.info("Streaming chat with model: {}, messages count: {}", model, messages.size());

        // TODO: 實作真正的 OpenAI API 串流呼叫
        // 目前返回模擬串流回覆
        String mockResponse = "這是一個模擬的 AI 回覆。在實際應用中，這裡會連接到 OpenAI API 並串流回傳真正的 AI 生成內容。";

        return Flux.fromArray(mockResponse.split(""))
                .map(LlmStreamEvent::delta)
                .delayElements(Duration.ofMillis(50))
                .doOnSubscribe(s -> log.debug("Starting mock stream"))
                .doOnComplete(() -> log.debug("Mock stream completed"));
    }

    @Override
    public String chat(List<Map<String, String>> messages, String model, double temperature, int maxTokens) {
        log.info("Chat with model: {}, messages count: {}", model, messages.size());

        // TODO: 實作真正的 OpenAI API 呼叫
        return "這是一個模擬的 AI 回覆。";
    }

    @Override
    public String getProviderName() {
        return "openai";
    }

    @Override
    public String getDefaultModel() {
        return defaultModel;
    }
}
