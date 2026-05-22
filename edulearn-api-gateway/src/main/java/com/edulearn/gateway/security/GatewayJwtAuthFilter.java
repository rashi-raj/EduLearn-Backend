package com.edulearn.gateway.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Gateway-level JWT Authentication Filter.
 *
 * Runs on every inbound request BEFORE routing to any downstream service.
 *
 * Responsibilities:
 *   1. Skip validation for whitelisted public paths
 *   2. Extract and validate the Bearer JWT from the Authorization header
 *   3. Reject with 401 if token is absent or invalid
 *   4. Inject trusted X-User-* headers for downstream services on success
 *
 * Future-proof: any new service added to application.yml routes is
 * automatically protected because SecurityConfig uses .anyRequest().authenticated()
 * which backs this filter.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GatewayJwtAuthFilter extends OncePerRequestFilter {

    private final GatewayJwtService jwtService;

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    /**
     * Public paths — these bypass JWT validation entirely.
     *
     * HOW TO ADD A NEW PUBLIC ENDPOINT:
     *   Simply add a new AntPath pattern to this list.
     *   Example: "/api/v1/some-new-service/public/**"
     */
    private static final List<String> PUBLIC_PATHS = List.of(
            // Auth endpoints — no token needed for login/register/password flows
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password",
            "/api/v1/auth/refresh-token",

            // Google OAuth2 flow — handled by Spring Security OAuth2
            "/oauth2/**",
            "/login/**",

            // Public course browsing — only GET is public
            "GET:/api/v1/courses",
            "GET:/api/v1/courses/**",

            // Public lesson previews — shown on course detail page before enrollment
            "/api/v1/lessons/course/{courseId}/preview",

            // Enrollment count — shown publicly on course detail page
            "/api/v1/enrollments/count/{courseId}",

            // Developer tooling — Swagger UI and API docs
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",

            // Health monitoring — actuator endpoints
            "/actuator/**",

            // CORS preflight — OPTIONS must never require a token
            "OPTIONS:/**"
    );

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String method = request.getMethod();
        String path   = request.getServletPath();

        // ── 1. Always pass CORS preflight requests ──────────────────────
        if ("OPTIONS".equalsIgnoreCase(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        // ── 2. Check public whitelist ────────────────────────────────────
        if (isPublicPath(method, path)) {
            log.debug("[GatewayFilter] Public path — skipping JWT check: {} {}", method, path);
            filterChain.doFilter(request, response);
            return;
        }

        // ── 3. Extract Authorization header ─────────────────────────────
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("[GatewayFilter] Rejected — missing or malformed Authorization header: {} {}", method, path);
            sendUnauthorized(response, "Missing or malformed Authorization header. Expected: 'Bearer <token>'");
            return;
        }

        String token = authHeader.substring(7); // Strip "Bearer " prefix

        // ── 4. Validate token ────────────────────────────────────────────
        if (!jwtService.isTokenValid(token)) {
            log.warn("[GatewayFilter] Rejected — invalid or expired JWT: {} {}", method, path);
            sendUnauthorized(response, "JWT token is invalid or has expired. Please log in again.");
            return;
        }

        // ── 5. Extract identity claims from token ────────────────────────
        String email = safeExtract(token, "email_from_subject");
        String role  = safeExtract(token, "role");

        // Subject holds the email (set by Auth Service JwtService)
        try {
            email = jwtService.extractEmail(token);
        } catch (Exception ignored) {
            // email stays null — downstream service should handle missing header gracefully
        }

        // ── 6. Inject X-User-* headers for downstream services ───────────
        // Downstream services can read these trusted headers directly
        // without re-validating the JWT (since gateway already verified it)
        MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest(request);

        if (email != null) {
            mutableRequest.putHeader("X-User-Email", email);
        }
        if (role != null) {
            mutableRequest.putHeader("X-User-Role", role);
        }
        // Always forward the original Authorization header so downstream
        // services that still do their own validation (Auth Service) continue to work
        mutableRequest.putHeader("X-Gateway-Verified", "true");

        log.debug("[GatewayFilter] Authorized — forwarding to downstream: {} {} | email={} | role={}",
                method, path, email, role);

        // ── 7. Set authentication in Spring Security Context ────────────
        // This is CRITICAL. Without this, Spring's FilterChain will still
        // see the request as unauthenticated and return 403 Forbidden.
        if (email != null) {
            List<SimpleGrantedAuthority> authorities;
            if (role != null && !role.trim().isEmpty()) {
                String formattedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                authorities = List.of(new SimpleGrantedAuthority(formattedRole));
                log.debug("[GatewayFilter] Setting authority: {}", formattedRole);
            } else {
                authorities = List.of();
                log.warn("[GatewayFilter] No role found for user: {}", email);
            }
                
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    email, null, authorities
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(mutableRequest, response);
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    /**
     * Check whether the request path matches any entry in PUBLIC_PATHS.
     * Supports Ant-style wildcards: **, *, ?
     */
    private boolean isPublicPath(String method, String path) {
        for (String pattern : PUBLIC_PATHS) {
            // Handle "METHOD:pattern" entries (e.g. OPTIONS:/**)
            if (pattern.contains(":")) {
                String[] parts = pattern.split(":", 2);
                if (parts[0].equalsIgnoreCase(method) && PATH_MATCHER.match(parts[1], path)) {
                    return true;
                }
            } else if (PATH_MATCHER.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    private String safeExtract(String token, String claim) {
        try {
            return jwtService.extractClaim(token, claim);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Write a structured 401 JSON response and stop the filter chain.
     */
    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                String.format("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"%s\"}", message)
        );
    }
}
