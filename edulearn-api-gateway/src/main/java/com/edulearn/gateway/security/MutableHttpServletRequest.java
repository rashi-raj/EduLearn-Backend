package com.edulearn.gateway.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Wrapper around HttpServletRequest that allows adding custom headers.
 *
 * HttpServletRequest is immutable by the Servlet spec — you cannot add headers
 * to an incoming request. This wrapper intercepts getHeader() calls and returns
 * our injected values, making X-User-* headers visible to downstream services
 * when the request is forwarded through the gateway filter chain.
 *
 * Usage:
 *   MutableHttpServletRequest mutable = new MutableHttpServletRequest(request);
 *   mutable.putHeader("X-User-Email", "user@example.com");
 *   filterChain.doFilter(mutable, response);
 */
public class MutableHttpServletRequest extends HttpServletRequestWrapper {

    private final Map<String, String> customHeaders = new HashMap<>();

    public MutableHttpServletRequest(HttpServletRequest request) {
        super(request);
    }

    /**
     * Add or override a request header.
     *
     * @param name  header name (case-insensitive in HTTP spec)
     * @param value header value
     */
    public void putHeader(String name, String value) {
        customHeaders.put(name.toLowerCase(), value);
    }

    @Override
    public String getHeader(String name) {
        String customValue = customHeaders.get(name.toLowerCase());
        if (customValue != null) {
            return customValue;
        }
        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        String customValue = customHeaders.get(name.toLowerCase());
        if (customValue != null) {
            return Collections.enumeration(Collections.singletonList(customValue));
        }
        return super.getHeaders(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        Set<String> names = new HashSet<>(customHeaders.keySet());
        Enumeration<String> originalNames = super.getHeaderNames();
        if (originalNames != null) {
            while (originalNames.hasMoreElements()) {
                names.add(originalNames.nextElement().toLowerCase());
            }
        }
        return Collections.enumeration(names);
    }
}
