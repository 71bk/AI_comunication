package tw.bk.ai.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import tw.bk.ai.result.Result;

/**
 * Ensure Result responses include path and traceId for successful responses.
 */
@RestControllerAdvice
public class ResultResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {

        if (body instanceof Result<?> result) {
            if (result.getPath() == null) {
                result.path(getPath(request));
            }
            if (result.getTraceId() == null) {
                result.traceId(getTraceId(request));
            }
        }

        return body;
    }

    private String getPath(ServerHttpRequest request) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest raw = servletRequest.getServletRequest();
            return raw.getRequestURI();
        }
        return request.getURI().getPath();
    }

    private String getTraceId(ServerHttpRequest request) {
        String traceId = MDC.get("traceId");
        if (traceId != null && !traceId.isBlank()) {
            return traceId;
        }
        if (request instanceof ServletServerHttpRequest servletRequest) {
            return servletRequest.getServletRequest().getHeader("X-Trace-Id");
        }
        return null;
    }
}
