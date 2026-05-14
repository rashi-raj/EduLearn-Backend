package com.edulearn.auth.security;

import com.edulearn.auth.entity.User;
import com.edulearn.auth.enums.ApprovalStatus;
import com.edulearn.auth.enums.AuthProvider;
import com.edulearn.auth.enums.Role;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter Unit Tests")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String VALID_TOKEN = "valid.jwt.token";

    private UserDetails buildUserDetails() {
        return new org.springframework.security.core.userdetails.User(
                "test@example.com",
                "encodedPassword",
                List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );
    }

    @BeforeEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Skips filter for /api/v1/auth/ paths")
    void doFilter_authPath_skipsJwtValidation() throws Exception {
        when(request.getServletPath()).thenReturn("/api/v1/auth/login");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtService, never()).extractUsername(any());
    }

    @Test
    @DisplayName("Skips filter for /oauth2/ paths")
    void doFilter_oauth2Path_skipsJwtValidation() throws Exception {
        when(request.getServletPath()).thenReturn("/oauth2/callback");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtService, never()).extractUsername(any());
    }

    @Test
    @DisplayName("Skips filter for /login/ paths")
    void doFilter_loginPath_skipsJwtValidation() throws Exception {
        when(request.getServletPath()).thenReturn("/login/oauth2/code/google");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtService, never()).extractUsername(any());
    }

    @Test
    @DisplayName("Skips filter when Authorization header is missing")
    void doFilter_missingAuthHeader_skipsJwtValidation() throws Exception {
        when(request.getServletPath()).thenReturn("/api/v1/admin/users/all");
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtService, never()).extractUsername(any());
    }

    @Test
    @DisplayName("Skips filter when Authorization header does not start with 'Bearer '")
    void doFilter_invalidAuthHeaderPrefix_skipsJwtValidation() throws Exception {
        when(request.getServletPath()).thenReturn("/api/v1/admin/users/all");
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtService, never()).extractUsername(any());
    }

    @Test
    @DisplayName("Sets authentication in context for valid Bearer token")
    void doFilter_validToken_setsAuthentication() throws Exception {
        when(request.getServletPath()).thenReturn("/api/v1/admin/users/all");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(jwtService.extractUsername(VALID_TOKEN)).thenReturn("test@example.com");

        UserDetails userDetails = buildUserDetails();
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid(VALID_TOKEN, userDetails)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("test@example.com",
                SecurityContextHolder.getContext().getAuthentication().getName());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Does not set authentication for invalid token")
    void doFilter_invalidToken_doesNotSetAuthentication() throws Exception {
        when(request.getServletPath()).thenReturn("/api/v1/admin/users/all");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(jwtService.extractUsername(VALID_TOKEN)).thenReturn("test@example.com");

        UserDetails userDetails = buildUserDetails();
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid(VALID_TOKEN, userDetails)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Does not set authentication when extracted username is null")
    void doFilter_nullUsername_doesNotSetAuthentication() throws Exception {
        when(request.getServletPath()).thenReturn("/api/v1/admin/users/all");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(jwtService.extractUsername(VALID_TOKEN)).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(userDetailsService, never()).loadUserByUsername(any());
        verify(filterChain, times(1)).doFilter(request, response);
    }
}
