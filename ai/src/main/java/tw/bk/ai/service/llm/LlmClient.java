package tw.bk.ai.service.llm;

import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * LLM 客戶端介面
 * 用於隔離不同供應商的實作差異（Port/Adapter 模式）
 */
public interface LlmClient {

    /**
     * 串流生成回覆
     *
     * @param messages    訊息歷史
     * @param model       模型名稱
     * @param temperature 溫度參數
     * @param maxTokens   最大 token 數
     * @return 串流回覆（每個 String 是一個文字片段）
     */
    Flux<LlmStreamEvent> streamChat(List<Map<String, String>> messages, String model, double temperature,
            int maxTokens);

    /**
     * 同步生成回覆（非串流）
     *
     * @param messages    訊息歷史
     * @param model       模型名稱
     * @param temperature 溫度參數
     * @param maxTokens   最大 token 數
     * @return 完整回覆內容
     */
    String chat(List<Map<String, String>> messages, String model, double temperature, int maxTokens);

    /**
     * 取得供應商名稱
     */
    String getProviderName();

    /**
     * 取得預設模型名稱
     */
    String getDefaultModel();
}
