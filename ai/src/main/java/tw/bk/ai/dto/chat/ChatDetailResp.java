package tw.bk.ai.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.bk.ai.entity.Chat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 對話詳情回應 DTO（含訊息）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatDetailResp {

    private Long id;
    private String title;
    private List<MessageResp> messages;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ChatDetailResp from(Chat chat) {
        return ChatDetailResp.builder()
                .id(chat.getId())
                .title(chat.getTitle())
                .messages(chat.getMessages().stream()
                        .map(MessageResp::from)
                        .collect(Collectors.toList()))
                .createdAt(chat.getCreatedAt())
                .updatedAt(chat.getUpdatedAt())
                .build();
    }
}
