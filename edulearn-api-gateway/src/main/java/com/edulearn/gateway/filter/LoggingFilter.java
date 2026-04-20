package com.edulearn.gateway.filter;

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

@Slf4j
@Component
@Order(1)
public class LoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String method = httpRequest.getMethod();
        String uri = httpRequest.getRequestURI();
        String queryString = httpRequest.getQueryString();
        String fullUri = queryString != null ? uri + "?" + queryString : uri;
        String clientIp = getClientIp(httpRequest);

        long startTime = System.currentTimeMillis();

        if (!shouldSkipLogging(uri)) {
            log.info(">>> Incoming Request | method={} | uri={} | ip={}", method, fullUri, clientIp);
        }

        try {
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = httpResponse.getStatus();

            if (!shouldSkipLogging(uri)) {
                if (status >= 500) {
                    log.error("<<< Outgoing Response | method={} | uri={} | status={} | duration={}ms",
                            method, fullUri, status, duration);
                } else if (status >= 400) {
                    log.warn("<<< Outgoing Response | method={} | uri={} | status={} | duration={}ms",
                            method, fullUri, status, duration);
                } else {
                    log.info("<<< Outgoing Response | method={} | uri={} | status={} | duration={}ms",
                            method, fullUri, status, duration);
                }
            }
        }
    }

    private boolean shouldSkipLogging(String uri) {
        return uri.startsWith("/actuator")
                || uri.startsWith("/swagger-ui")
                || uri.startsWith("/v3/api-docs")
                || uri.contains("favicon.ico");
    }

    private String getClientIp(HttpServletRequest request) {
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