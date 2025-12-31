package tw.bk.ai.service.llm;

/**
 * LLM streaming event payload.
 */
public class LlmStreamEvent {

    private final String delta;
    private final Integer inputTokens;
    private final Integer outputTokens;

    public LlmStreamEvent(String delta, Integer inputTokens, Integer outputTokens) {
        this.delta = delta;
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
    }

    public static LlmStreamEvent delta(String delta) {
        return new LlmStreamEvent(delta, null, null);
    }

    public static LlmStreamEvent usage(Integer inputTokens, Integer outputTokens) {
        return new LlmStreamEvent(null, inputTokens, outputTokens);
    }

    public String getDelta() {
        return delta;
    }

    public Integer getInputTokens() {
        return inputTokens;
    }

    public Integer getOutputTokens() {
        return outputTokens;
    }
}
