package tw.bk.ai.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 統一 API 回應格式
 * 所有 API 回應都使用此格式封裝
 *
 * @param <T> 回應資料類型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 狀態碼（對應 ErrorCode enum）
     */
    private String code;

    /**
     * 訊息
     */
    private String message;

    /**
     * 回應資料
     */
    private T data;

    /**
     * 請求路徑
     */
    private String path;

    /**
     * 時間戳
     */
    private OffsetDateTime timestamp;

    /**
     * 追蹤 ID（用於日誌追查）
     */
    private String traceId;

    // ========== 靜態工廠方法 ==========

    public static <T> Result<T> ok(T data) {
        return Result.<T>builder()
                .success(true)
                .code("OK")
                .message("success")
                .data(data)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> fail(String code, String message) {
        return Result.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    public static <T> Result<T> fail(String code, String message, T data) {
        return Result.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .data(data)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    // ========== 鏈式設定方法 ==========

    public Result<T> path(String path) {
        this.path = path;
        return this;
    }

    public Result<T> traceId(String traceId) {
        this.traceId = traceId;
        return this;
    }
}
