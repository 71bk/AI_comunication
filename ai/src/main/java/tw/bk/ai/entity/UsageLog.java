package tw.bk.ai.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Usage log entity.
 */
@Entity
@Table(name = "usage_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id")
    private Chat chat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    private Message message;

    @Column(nullable = false, length = 50)
    private String provider;

    @Column(nullable = false, length = 100)
    private String model;

    @Column(name = "token_in", nullable = false)
    @Builder.Default
    private Integer tokenIn = 0;

    @Column(name = "token_out", nullable = false)
    @Builder.Default
    private Integer tokenOut = 0;

    @Column(name = "cost_usd", precision = 10, scale = 6)
    private BigDecimal costUsd;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Long getUserId() {
        return user != null ? user.getId() : null;
    }

    public Long getChatId() {
        return chat != null ? chat.getId() : null;
    }

    public Long getMessageId() {
        return message != null ? message.getId() : null;
    }
}
