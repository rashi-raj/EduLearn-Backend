package com.edulearn.gateway.config;

import com.edulearn.gateway.security.GatewayJwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Gateway Security Configuration.
 *
 * Security model:
 *   - The gateway is the SINGLE entry point. All JWT validation happens here.
 *   - Downstream services receive trusted X-User-* headers and can skip JWT re-validation.
 *   - Auth Service keeps its own JWT filter for defense-in-depth (two layers).
 *
 * Adding a new protected service:
 *   → Just add its route to application.yml. No changes needed here.
 *     The ".anyRequest().authenticated()" rule covers all routes automatically.
 *
 * Adding a new PUBLIC endpoint for a new service:
 *   → Add its path to GatewayJwtAuthFilter.PUBLIC_PATHS list.
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final GatewayJwtAuthFilter gatewayJwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ── CORS — allow Angular frontend ──────────────────────────────
                .cors(Customizer.withDefaults())

                // ── CSRF — disabled (stateless JWT, no browser sessions) ────────
                .csrf(AbstractHttpConfigurer::disable)

                // ── Session — stateless, JWT handles identity ───────────────────
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // ── URL Authorization Rules ────────────────────────────────────
                .authorizeHttpRequests(auth -> auth

                        // CORS preflight — must always pass, no token
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Auth endpoints — public, token not yet issued
                        .requestMatchers(
                                "/api/v1/auth/register",
                                "/api/v1/auth/login",
                                "/api/v1/auth/forgot-password",
                                "/api/v1/auth/reset-password",
                                "/api/v1/auth/refresh-token"
                        ).permitAll()

                        // Google OAuth2 — handled by OAuth2 flow, no Bearer token
                        .requestMatchers("/oauth2/**", "/login/**").permitAll()

                        // Public browsing
                        .requestMatchers(HttpMethod.GET, "/api/v1/courses", "/api/v1/courses/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/lessons/course/*/preview").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/enrollments/count/*").permitAll()

                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/actuator/**"
                        ).permitAll()

                        .anyRequest().authenticated()
                )

                // ── JWT Filter — runs before Spring's auth filter ──────────────
                // GatewayJwtAuthFilter validates the token and injects X-User-* headers.
                // If token is invalid it returns 401 and the request never reaches downstream.
                .addFilterBefore(gatewayJwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}