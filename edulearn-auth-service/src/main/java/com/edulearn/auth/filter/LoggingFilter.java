package com.edulearn.auth.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Servlet filter that logs every inbound HTTP request and its corresponding response.
 *
 * <p>Requests arriving here have already been forwarded by the API Gateway (port 8080).
 * The original client IP is extracted from the {@code X-Forwarded-For} header set by the gateway.</p>
 *
 * <p>Log levels:
 * <ul>
 *   <li>5xx → ERROR</li>
 *   <li>4xx → WARN</li>
 *   <li>2xx / 3xx → INFO</li>
 * </ul>
 */
@Slf4j
@Component
@Order(1)
public class LoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  httpReq  = (HttpServletRequest)  request;
        HttpServletResponse httpResp = (HttpServletResponse) response;

        String method      = httpReq.getMethod();
        String uri         = httpReq.getRequestURI();
        String queryString = httpReq.getQueryString();
        String fullUri     = queryString != null ? uri + "?" + queryString : uri;
        String clientIp    = resolveClientIp(httpReq);

        // Whether request came through the gateway
        String forwardedFor = httpReq.getHeader("X-Forwarded-For");
        String via          = forwardedFor != null ? " (via gateway, origin=" + forwardedFor + ")" : " (direct)";

        long startTime = System.currentTimeMillis();
        log.info(">>> Incoming Request  | method={} | uri={} | ip={}{}", method, fullUri, clientIp, via);

        try {
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int  status   = httpResp.getStatus();

            if (status >= 500) {
                log.error("<<< Outgoing Response | method={} | uri={} | status={} | duration={}ms",
                        method, fullUri, status, duration);
            } else if (status >= 400) {
                log.warn("<<< Outgoing Response  | method={} | uri={} | status={} | duration={}ms",
                        method, fullUri, status, duration);
            } else {
                log.info("<<< Outgoing Response  | method={} | uri={} | status={} | duration={}ms",
                        method, fullUri, status, duration);
            }
        }
    }

    /**
     * Resolves the real client IP, honouring proxy headers set by the API Gateway.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
