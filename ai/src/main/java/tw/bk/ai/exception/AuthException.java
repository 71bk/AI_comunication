package tw.bk.ai.exception;

/**
 * 認證異常
 */
public class AuthException extends BizException {

    public AuthException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AuthException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public static AuthException invalidCredentials() {
        return new AuthException(ErrorCode.AUTH_INVALID_CREDENTIALS);
    }

    public static AuthException invalidToken() {
        return new AuthException(ErrorCode.AUTH_INVALID_TOKEN);
    }

    public static AuthException tokenExpired() {
        return new AuthException(ErrorCode.AUTH_TOKEN_EXPIRED);
    }

    public static AuthException forbidden() {
        return new AuthException(ErrorCode.AUTH_FORBIDDEN);
    }

    public static AuthException userExists(String email) {
        return new AuthException(ErrorCode.AUTH_USER_EXISTS, "Email already registered: " + email);
    }
}
