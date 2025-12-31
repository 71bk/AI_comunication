package tw.bk.ai.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 訊息實體
 */
@Entity
@Table(name = "messages")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageRole role;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(length = 50)
    private String provider;

    @Column(length = 100)
    private String model;

    @Column(name = "token_in")
    private Integer tokenIn;

    @Column(name = "token_out")
    private Integer tokenOut;

    /**
     * 擴充資料（B 版用於 citations 等）
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata_json", columnDefinition = "JSON")
    private Map<String, Object> metadata;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum MessageRole {
        system, user, assistant
    }

    // ========== 便捷方法 ==========

    public Long getChatId() {
        return chat != null ? chat.getId() : null;
    }

    public static Message userMessage(String content) {
        return Message.builder()
                .role(MessageRole.user)
                .content(content)
                .build();
    }

    public static Message assistantMessage(String content) {
        return Message.builder()
                .role(MessageRole.assistant)
                .content(content)
                .build();
    }

    public static Message systemMessage(String content) {
        return Message.builder()
                .role(MessageRole.system)
                .content(content)
                .build();
    }
}
