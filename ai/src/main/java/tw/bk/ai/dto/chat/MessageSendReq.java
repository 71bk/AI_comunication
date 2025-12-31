package tw.bk.ai.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 發送訊息請求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSendReq {

    @NotBlank(message = "Content is required")
    @Size(max = 32000, message = "Content must be at most 32000 characters")
    private String content;

    /**
     * 模型名稱（可選，覆蓋預設）
     */
    private String model;

    /**
     * 溫度參數（可選）
     */
    private Double temperature;

    /**
     * 最大 token 數（可選）
     */
    private Integer maxTokens;
}
