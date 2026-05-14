package com.edulearn.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoggingFilter Unit Tests")
class LoggingFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private LoggingFilter loggingFilter;

    @BeforeEach
    void setUp() {
        lenient().when(request.getMethod()).thenReturn("GET");
        lenient().when(request.getRequestURI()).thenReturn("/api/v1/auth/ping");
        lenient().when(request.getQueryString()).thenReturn(null);
        lenient().when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        lenient().when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    }

    @Test
    @DisplayName("Processes request and calls chain.doFilter for a 200 response")
    void doFilter_200_logsInfo() throws Exception {
        when(response.getStatus()).thenReturn(200);

        loggingFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Processes request with query string correctly")
    void doFilter_withQueryString_logsFullUri() throws Exception {
        when(request.getQueryString()).thenReturn("token=abc");
        when(response.getStatus()).thenReturn(200);

        loggingFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Logs WARN for 4xx response codes")
    void doFilter_4xxResponse_logsWarn() throws Exception {
        when(response.getStatus()).thenReturn(404);

        loggingFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Logs ERROR for 5xx response codes")
    void doFilter_5xxResponse_logsError() throws Exception {
        when(response.getStatus()).thenReturn(500);

        loggingFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Extracts client IP from X-Forwarded-For header when available")
    void doFilter_withXForwardedFor_usesForwardedIp() throws Exception {
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.100, 10.0.0.1");
        when(response.getStatus()).thenReturn(200);

        loggingFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Extracts client IP from X-Real-IP header when X-Forwarded-For is absent")
    void doFilter_withXRealIp_usesRealIp() throws Exception {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn("10.20.30.40");
        when(response.getStatus()).thenReturn(200);

        loggingFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Falls back to getRemoteAddr when no proxy headers are present")
    void doFilter_noProxyHeaders_usesRemoteAddr() throws Exception {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(response.getStatus()).thenReturn(200);

        loggingFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(request, atLeastOnce()).getRemoteAddr();
    }

    @Test
    @DisplayName("Adds 'via gateway' annotation when X-Forwarded-For is present in request log")
    void doFilter_gatewayRequest_logsViaGateway() throws Exception {
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.5");
        when(response.getStatus()).thenReturn(201);

        loggingFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }
}
