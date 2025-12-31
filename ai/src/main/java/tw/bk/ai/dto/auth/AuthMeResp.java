package tw.bk.ai.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.bk.ai.entity.User;

import java.time.LocalDateTime;

/**
 * 認證用戶資訊回應 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthMeResp {

    private Long id;
    private String email;
    private String displayName;
    private String status;
    private String token;
    private LocalDateTime createdAt;

    public static AuthMeResp from(User user) {
        return AuthMeResp.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public static AuthMeResp from(User user, String token) {
        AuthMeResp resp = from(user);
        resp.setToken(token);
        return resp;
    }
}
