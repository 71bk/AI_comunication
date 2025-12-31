package tw.bk.ai.dto.chat;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 建立對話請求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatCreateReq {

    @Size(max = 255, message = "Title must be at most 255 characters")
    private String title;
}
