package tw.bk.ai.exception;

/**
 * 限流異常
 */
public class RateLimitException extends BizException {

    public RateLimitException() {
        super(ErrorCode.RATE_LIMITED);
    }

    public RateLimitException(String message) {
        super(ErrorCode.RATE_LIMITED, message);
    }
}
