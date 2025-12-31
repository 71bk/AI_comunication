package tw.bk.ai.vo.chat;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.bk.ai.result.Result;

import java.util.List;

/**
 * SSE 串流事件 VO
 * 用於封裝 SSE 事件的 data payload
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StreamEventVo {

    /**
     * 事件類型：delta, meta, done, error
     */
    private String type;

    /**
     * 文字片段（delta 事件）
     */
    private String delta;

    /**
     * 引用資訊（meta 事件，B 版用）
     */
    private List<Citation> citations;

    /**
     * Token 使用量（done 事件）
     */
    private Usage usage;

    /**
     * 錯誤資訊（error 事件）
     */
    private Result<Object> error;

    // ========== 靜態工廠方法 ==========

    public static StreamEventVo delta(String text) {
        return StreamEventVo.builder()
                .type("delta")
                .delta(text)
                .build();
    }

    public static StreamEventVo meta(List<Citation> citations) {
        return StreamEventVo.builder()
                .type("meta")
                .citations(citations)
                .build();
    }

    public static StreamEventVo done(int inputTokens, int outputTokens) {
        return StreamEventVo.builder()
                .type("done")
                .usage(new Usage(inputTokens, outputTokens))
                .build();
    }

    public static StreamEventVo error(Result<Object> errorResult) {
        return StreamEventVo.builder()
                .type("error")
                .error(errorResult)
                .build();
    }

    // ========== 內部類別 ==========

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Citation {
        private Long docId;
        private Long chunkId;
        private String title;
        private Integer page;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage {
        private int inputTokens;
        private int outputTokens;
    }
}
