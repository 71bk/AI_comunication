package tw.bk.ai.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * TraceId 過濾器
 * 為每個請求生成唯一的追蹤 ID，方便日誌追查
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String TRACE_ID_KEY = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            // 嘗試從請求頭取得 traceId，若無則生成新的
            String traceId = request.getHeader(TRACE_ID_HEADER);
            if (traceId == null || traceId.isBlank()) {
                traceId = generateTraceId();
            }

            // 放入 MDC 供日誌使用
            MDC.put(TRACE_ID_KEY, traceId);

            // 在回應頭中返回 traceId
            response.setHeader(TRACE_ID_HEADER, traceId);

            filterChain.doFilter(request, response);
        } finally {
            // 清除 MDC，避免線程池污染
            MDC.remove(TRACE_ID_KEY);
        }
    }

    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
