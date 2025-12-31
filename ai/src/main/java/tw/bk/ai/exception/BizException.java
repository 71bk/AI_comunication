package tw.bk.ai.exception;

import lombok.Getter;

/**
 * 業務異常基類
 * 所有業務相關的異常都繼承此類
 */
@Getter
public class BizException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Object details;

    public BizException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = null;
    }

    public BizException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.details = null;
    }

    public BizException(ErrorCode errorCode, Object details) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = details;
    }

    public BizException(ErrorCode errorCode, String message, Object details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }

    public String getCode() {
        return errorCode.getCode();
    }
}
