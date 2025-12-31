package tw.bk.ai.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.bk.ai.entity.Chat;

import java.time.LocalDateTime;

/**
 * 對話列表項目回應 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResp {

    private Long id;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ChatResp from(Chat chat) {
        return ChatResp.builder()
                .id(chat.getId())
                .title(chat.getTitle())
                .createdAt(chat.getCreatedAt())
                .updatedAt(chat.getUpdatedAt())
                .build();
    }
}
