package com.edulearn.auth.config;

import com.edulearn.auth.security.JwtAuthenticationFilter;
import com.edulearn.auth.security.OAuth2LoginSuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
//Enables method-level security, for example @PreAuthorize("hasAuthority('ROLE_ADMIN')").
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    // Your custom filter that checks JWT tokens in requests.
    private final UserDetailsService userDetailsService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    // Handler that runs after Google OAuth login succeeds.

    // This method defines the main HTTP security rules for the auth service.
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            OAuth2AuthorizationRequestResolver customOAuth2AuthorizationRequestResolver
    ) throws Exception {
        http
        		// Enables CORS using the corsConfigurationSource bean defined below.
        		.cors(Customizer.withDefaults())
        		// Disables CSRF protection because this project uses JWT tokens instead of form sessions.
                .csrf(AbstractHttpConfigurer::disable)
                // Starts defining which URLs are public and which need authentication.
                // These URLs are public. Anyone can access them without a JWT.
                // /api/v1/auth/** includes login, register, forgot password, reset password.
                // /oauth2/** and /login/** are needed for Google login.
                // Swagger and actuator are allowed publicly here.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/oauth2/**",
                                "/login/**",
                                "/actuator/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        // Every other request must be authenticated.
                        .anyRequest().authenticated()
                )
                // Enables OAuth2 login, used here for Google login.
                .oauth2Login(oauth -> oauth
                		// Customizes the Google OAuth request.
                        // project uses this to store selected role/mode during Google login.
                        .authorizationEndpoint(authorization ->
                                authorization.authorizationRequestResolver(customOAuth2AuthorizationRequestResolver)
                        )
                        // After Google login succeeds, this handler creates/fetches user and generates JWT.
                        .successHandler(oAuth2LoginSuccessHandler)
                )
                // Defines what should happen when authentication fails.
                .exceptionHandling(ex -> ex
                		// This block runs when an unauthenticated user tries to access a protected API.
                        .authenticationEntryPoint((request, response, authException) -> {
                        	// If the request is an API request...
                            if (request.getRequestURI().startsWith("/api/")) {
                            	// Set HTTP status code to 401 Unauthorized.
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                // Tell client the response is JSON.
                                response.setContentType("application/json");
                                // Send a simple JSON error message.
                                response.getWriter().write("{\"message\":\"Unauthorized\"}");
                            } else {
                            	// If the request is not an API request...
                            	// Send normal 401 error response.
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                            }
                        })
                )
                // Allows sessions only if required.
                // This is needed because OAuth2 login temporarily uses session during Google flow.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                
                // Registers your custom authentication provider.
                .authenticationProvider(authenticationProvider())
                
                // Adds your JWT filter before Spring’s default username/password filter.
                // Meaning: JWT is checked early in the security process.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // Builds and returns the complete security filter chain.
        return http.build();
    }

    
    // This method creates the object that customizes Google OAuth login requests.
    @Bean
    public OAuth2AuthorizationRequestResolver customOAuth2AuthorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository
    ) {
        return new CustomOAuth2AuthorizationRequestResolver(clientRegistrationRepository);
    }

    
    // This method defines which frontend origins and headers are allowed.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:4200",
                "https://edulearnweb.duckdns.org",
                "http://edulearnweb.duckdns.org"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        // Explicit headers — wildcard not allowed without allowCredentials, keep both consistent
        configuration.setAllowedHeaders(List.of(
                "Authorization", "Content-Type", "Accept",
                "X-Requested-With", "X-User-Email", "X-User-Role", "X-Gateway-Verified"
        ));
        configuration.setExposedHeaders(List.of("Authorization"));

        // Creates a source object where CORS rules can be registered.
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Applies this CORS configuration to all URLs.
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}