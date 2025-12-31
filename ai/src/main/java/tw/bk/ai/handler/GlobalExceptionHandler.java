package tw.bk.ai.handler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tw.bk.ai.exception.*;
import tw.bk.ai.result.Result;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全域異常處理器
 * 統一攔截所有異常並轉換為 Result 格式
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String TRACE_ID_KEY = "traceId";

    /**
     * 處理業務異常
     */
    @ExceptionHandler(BizException.class)
    public ResponseEntity<Result<Object>> handleBizException(BizException ex, HttpServletRequest request) {
        log.warn("Business exception: {} - {}", ex.getCode(), ex.getMessage());

        Result<Object> result = Result.fail(ex.getCode(), ex.getMessage(), ex.getDetails())
                .path(request.getRequestURI())
                .traceId(MDC.get(TRACE_ID_KEY));

        HttpStatus status = determineHttpStatus(ex.getErrorCode());
        return ResponseEntity.status(status).body(result);
    }

    /**
     * 處理 Validation 異常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Object>> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<Map<String, String>> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::mapFieldError)
                .collect(Collectors.toList());

        Map<String, Object> details = new HashMap<>();
        details.put("fields", fieldErrors);

        log.warn("Validation failed: {}", fieldErrors);

        Result<Object> result = Result.fail(
                ErrorCode.VALIDATION_FAILED.getCode(),
                ErrorCode.VALIDATION_FAILED.getMessage(),
                (Object) details)
                .path(request.getRequestURI())
                .traceId(MDC.get(TRACE_ID_KEY));

        return ResponseEntity.badRequest().body(result);
    }

    /**
     * 處理認證異常
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Result<Object>> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {

        log.warn("Authentication failed: {}", ex.getMessage());

        Result<Object> result = Result.fail(
                ErrorCode.AUTH_INVALID_CREDENTIALS.getCode(),
                ex.getMessage())
                .path(request.getRequestURI())
                .traceId(MDC.get(TRACE_ID_KEY));

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
    }

    /**
     * 處理授權異常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Result<Object>> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {

        log.warn("Access denied: {}", ex.getMessage());

        Result<Object> result = Result.fail(
                ErrorCode.AUTH_FORBIDDEN.getCode(),
                ErrorCode.AUTH_FORBIDDEN.getMessage())
                .path(request.getRequestURI())
                .traceId(MDC.get(TRACE_ID_KEY));

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
    }

    /**
     * 處理未知異常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Object>> handleException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error: ", ex);

        Result<Object> result = Result.fail(
                ErrorCode.INTERNAL_ERROR.getCode(),
                "An unexpected error occurred")
                .path(request.getRequestURI())
                .traceId(MDC.get(TRACE_ID_KEY));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }

    // ========== 輔助方法 ==========

    private Map<String, String> mapFieldError(FieldError fieldError) {
        Map<String, String> error = new HashMap<>();
        error.put("field", fieldError.getField());
        error.put("reason", fieldError.getDefaultMessage());
        return error;
    }

    private HttpStatus determineHttpStatus(ErrorCode errorCode) {
        return switch (errorCode) {
            case AUTH_INVALID_CREDENTIALS, AUTH_INVALID_TOKEN, AUTH_TOKEN_EXPIRED -> HttpStatus.UNAUTHORIZED;
            case AUTH_FORBIDDEN, CHAT_ACCESS_DENIED -> HttpStatus.FORBIDDEN;
            case CHAT_NOT_FOUND, MESSAGE_NOT_FOUND, USER_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case RATE_LIMITED -> HttpStatus.TOO_MANY_REQUESTS;
            case VALIDATION_FAILED, BAD_REQUEST -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
