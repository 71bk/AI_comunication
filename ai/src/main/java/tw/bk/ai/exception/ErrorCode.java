package tw.bk.ai.exception;

import lombok.Getter;

/**
 * 錯誤碼枚舉
 * 統一定義所有業務錯誤碼
 */
@Getter
public enum ErrorCode {

    // ========== 通用錯誤 ==========
    OK("OK", "Success"),
    VALIDATION_FAILED("VALIDATION_FAILED", "Validation failed"),
    INTERNAL_ERROR("INTERNAL_ERROR", "Internal server error"),
    BAD_REQUEST("BAD_REQUEST", "Bad request"),

    // ========== 認證相關 ==========
    AUTH_INVALID_CREDENTIALS("AUTH_INVALID_CREDENTIALS", "Invalid email or password"),
    AUTH_INVALID_TOKEN("AUTH_INVALID_TOKEN", "Invalid or expired token"),
    AUTH_TOKEN_EXPIRED("AUTH_TOKEN_EXPIRED", "Token has expired"),
    AUTH_FORBIDDEN("AUTH_FORBIDDEN", "Access denied"),
    AUTH_USER_EXISTS("AUTH_USER_EXISTS", "User already exists"),

    // ========== 對話相關 ==========
    CHAT_NOT_FOUND("CHAT_NOT_FOUND", "Chat not found"),
    CHAT_ACCESS_DENIED("CHAT_ACCESS_DENIED", "Access to chat denied"),
    MESSAGE_NOT_FOUND("MESSAGE_NOT_FOUND", "Message not found"),

    // ========== LLM 相關 ==========
    LLM_TIMEOUT("LLM_TIMEOUT", "Model request timeout"),
    LLM_PROVIDER_ERROR("LLM_PROVIDER_ERROR", "LLM provider error"),
    LLM_QUOTA_EXCEEDED("LLM_QUOTA_EXCEEDED", "LLM quota exceeded"),
    LLM_STREAM_ERROR("LLM_STREAM_ERROR", "Stream processing error"),

    // ========== 限流相關 ==========
    RATE_LIMITED("RATE_LIMITED", "Too many requests, please try again later"),

    // ========== 用戶相關 ==========
    USER_NOT_FOUND("USER_NOT_FOUND", "User not found");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
