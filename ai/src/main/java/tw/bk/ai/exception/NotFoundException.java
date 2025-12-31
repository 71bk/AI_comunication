package tw.bk.ai.exception;

/**
 * 資源未找到異常
 */
public class NotFoundException extends BizException {

    public NotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public NotFoundException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public NotFoundException(ErrorCode errorCode, Object details) {
        super(errorCode, details);
    }

    public static NotFoundException chat(Long chatId) {
        return new NotFoundException(ErrorCode.CHAT_NOT_FOUND, "chatId=" + chatId);
    }

    public static NotFoundException user(Long userId) {
        return new NotFoundException(ErrorCode.USER_NOT_FOUND, "userId=" + userId);
    }

    public static NotFoundException message(Long messageId) {
        return new NotFoundException(ErrorCode.MESSAGE_NOT_FOUND, "messageId=" + messageId);
    }
}
