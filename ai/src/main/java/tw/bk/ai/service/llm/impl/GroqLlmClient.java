package tw.bk.ai.service.llm.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import tw.bk.ai.service.llm.LlmClient;
import tw.bk.ai.service.llm.LlmStreamEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Groq LLM client (OpenAI-compatible API).
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.llm.provider", havingValue = "groq")
public class GroqLlmClient implements LlmClient {

    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${app.llm.api-key:}")
    private String apiKey;

    @Value("${app.llm.base-url:https://api.groq.com/openai/v1}")
    private String baseUrl;

    @Value("${app.llm.model:llama-3.1-8b-instant}")
    private String defaultModel;

    @Value("${app.llm.top-p:1}")
    private double topP;

    @Value("${app.llm.reasoning-effort:}")
    private String reasoningEffort;

    @Value("${app.llm.max-completion-tokens:}")
    private Integer maxCompletionTokens;

    @Override
    public Flux<LlmStreamEvent> streamChat(List<Map<String, String>> messages, String model, double temperature,
            int maxTokens) {
        return Flux.create(sink -> {
            HttpRequest request;
            try {
                String payload = buildPayload(messages, model, temperature, maxTokens, true);
                request = HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl + "/chat/completions"))
                        .timeout(Duration.ofSeconds(60))
                        .header("Authorization", "Bearer " + apiKey)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(payload))
                        .build();
            } catch (Exception ex) {
                sink.error(ex);
                return;
            }

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                    .thenAccept(response -> {
                        if (response.statusCode() < 200 || response.statusCode() >= 300) {
                            sink.error(new IllegalStateException("Groq API error: " + response.statusCode()));
                            return;
                        }

                        try (BufferedReader reader = new BufferedReader(
                                new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                if (line.isBlank() || !line.startsWith("data:")) {
                                    continue;
                                }
                                String data = line.substring(5).trim();
                                if ("[DONE]".equals(data)) {
                                    sink.complete();
                                    break;
                                }
                                LlmStreamEvent event = extractEvent(data);
                                if (event != null) {
                                    sink.next(event);
                                }
                            }
                        } catch (Exception ex) {
                            sink.error(ex);
                        }
                    })
                    .exceptionally(ex -> {
                        sink.error(ex);
                        return null;
                    });
        });
    }

    @Override
    public String chat(List<Map<String, String>> messages, String model, double temperature, int maxTokens) {
        try {
            String payload = buildPayload(messages, model, temperature, maxTokens, false);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .timeout(Duration.ofSeconds(60))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Groq API error: " + response.statusCode());
            }

            JsonNode root = objectMapper.readTree(response.body());
            return root.path("choices").path(0).path("message").path("content").asText("");
        } catch (Exception ex) {
            throw new IllegalStateException("Groq API error", ex);
        }
    }

    @Override
    public String getProviderName() {
        return "groq";
    }

    @Override
    public String getDefaultModel() {
        return defaultModel;
    }

    private String buildPayload(List<Map<String, String>> messages, String model, double temperature,
            int maxTokens, boolean stream) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", model != null ? model : defaultModel);
        payload.put("messages", messages);
        payload.put("temperature", temperature);
        if (maxCompletionTokens != null) {
            payload.put("max_completion_tokens", maxCompletionTokens);
        } else {
            payload.put("max_tokens", maxTokens);
        }
        payload.put("stream", stream);
        if (stream) {
            Map<String, Object> streamOptions = new HashMap<>();
            streamOptions.put("include_usage", true);
            payload.put("stream_options", streamOptions);
        }
        if (topP > 0) {
            payload.put("top_p", topP);
        }
        if (reasoningEffort != null && !reasoningEffort.isBlank()) {
            payload.put("reasoning_effort", reasoningEffort);
        }
        return objectMapper.writeValueAsString(payload);
    }

    private LlmStreamEvent extractEvent(String data) throws Exception {
        JsonNode root = objectMapper.readTree(data);
        String deltaText = null;
        JsonNode delta = root.path("choices").path(0).path("delta").path("content");
        if (!delta.isMissingNode() && !delta.isNull()) {
            deltaText = delta.asText();
        }

        Integer inputTokens = null;
        Integer outputTokens = null;
        JsonNode usage = root.path("usage");
        if (!usage.isMissingNode() && !usage.isNull()) {
            if (usage.has("prompt_tokens")) {
                inputTokens = usage.get("prompt_tokens").asInt();
            }
            if (usage.has("completion_tokens")) {
                outputTokens = usage.get("completion_tokens").asInt();
            }
        }

        if (deltaText == null && inputTokens == null && outputTokens == null) {
            return null;
        }
        return new LlmStreamEvent(deltaText, inputTokens, outputTokens);
    }
}
