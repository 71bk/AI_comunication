package tw.bk.ai.service.llm;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tw.bk.ai.entity.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Prompt 建構器
 * 集中管理 prompt 的組裝邏輯
 */
@Component
@RequiredArgsConstructor
public class PromptBuilder {

    private static final String DEFAULT_SYSTEM_PROMPT = """
            你是一個友善、清楚、務實的 AI 助手。請用繁體中文回答，語氣自然但不油。

            【回覆原則】
            - 先理解問題，再回答。若資訊不足，先提出 1～3 個關鍵追問；但若可以在合理假設下回答，先給可行方案再補追問。
            - 優先給可執行的步驟、範例與注意事項；避免空泛描述。
            - 對不確定的內容要明確說「我不確定」或「需要更多資訊」；不要編造。
            - 若使用者要求程式/設定步驟，優先用條列＋必要的程式碼區塊。

            【格式】
            - 默認用：短段落 + 條列清單
            - 需要展示資料結構時，使用 JSON 或表格（如果合適）

            【安全與界線】
            - 不提供違法、危害或入侵類的具體教學。
            - 涉及個資或敏感資料時提醒風險，避免要求使用者貼出秘密金鑰或密碼。

            【文件引用（RAG）規則】
            你可能會收到一段「文件內容摘錄（Context）」與其來源資訊（例如 docId、title、page、chunkId）。請嚴格遵守：

            1) 只能根據「Context」中有明確依據的內容作答。
            2) 若問題超出 Context，請清楚說「文件中沒有找到相關內容」，並提出下一步建議（例如：請提供更多文件、改問法、或我可以用一般知識回答但不保證與文件一致）。
            3) 回答中凡是引用文件資訊的句子，必須在句尾標註引用來源。
            4) 引用格式統一使用方括號，內容為： [來源: <title> p.<page> #<chunkId>]
               - 若沒有 page，改用： [來源: <title> #<chunkId>]
               - 若 title 缺失，用 docId： [來源: doc:<docId> p.<page> #<chunkId>]
            5) 若同一句包含多個來源，列出多個引用，例如：
               [來源: A p.3 #12][來源: B p.1 #5]
            6) 不要捏造引用（包含 page、chunkId、title），引用必須來自系統提供的來源資訊。
            7) 若 Context 互相矛盾：
               - 明確指出矛盾點
               - 優先採用較新或更具體的內容（若 Context 有日期/版本資訊）
               - 仍不足時，請要求使用者提供更多資訊或確認版本
            """;

    /**
     * 從訊息實體列表建構 prompt
     */
    public List<Map<String, String>> build(List<Message> messages) {
        return build(messages, null);
    }

    /**
     * 從訊息實體列表建構 prompt（含額外 context，備用於 RAG）
     *
     * @param messages 訊息列表
     * @param context  額外的上下文（B 版 RAG 用）
     */
    public List<Map<String, String>> build(List<Message> messages, String context) {
        List<Map<String, String>> prompt = new ArrayList<>();

        // 添加系統提示
        String systemPrompt = DEFAULT_SYSTEM_PROMPT;
        if (context != null && !context.isBlank()) {
            systemPrompt = systemPrompt + "\n\nRelevant context:\n" + context;
        }
        prompt.add(createMessage("system", systemPrompt));

        // 添加對話歷史
        for (Message message : messages) {
            String role = message.getRole().name();
            prompt.add(createMessage(role, message.getContent()));
        }

        return prompt;
    }

    /**
     * 從原始訊息列表建構（用於直接傳入）
     */
    public List<Map<String, String>> buildFromRaw(List<Map<String, String>> messages) {
        List<Map<String, String>> prompt = new ArrayList<>();

        // 檢查是否已有系統訊息
        boolean hasSystemMessage = messages.stream()
                .anyMatch(m -> "system".equals(m.get("role")));

        if (!hasSystemMessage) {
            prompt.add(createMessage("system", DEFAULT_SYSTEM_PROMPT));
        }

        prompt.addAll(messages);
        return prompt;
    }

    private Map<String, String> createMessage(String role, String content) {
        Map<String, String> message = new HashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }
}
