package tw.bk.ai.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.bk.ai.entity.Message;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 訊息回應 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResp {

    private Long id;
    private String role;
    private String content;
    private String provider;
    private String model;
    private Integer tokenIn;
    private Integer tokenOut;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;

    public static MessageResp from(Message message) {
        return MessageResp.builder()
                .id(message.getId())
                .role(message.getRole().name())
                .content(message.getContent())
                .provider(message.getProvider())
                .model(message.getModel())
                .tokenIn(message.getTokenIn())
                .tokenOut(message.getTokenOut())
                .metadata(message.getMetadata())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
