package tw.bk.ai.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Update chat title request DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatTitleUpdateReq {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be at most 255 characters")
    private String title;
}
