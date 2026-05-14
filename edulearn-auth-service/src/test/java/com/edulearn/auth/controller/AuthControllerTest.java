package com.edulearn.auth.controller;

import com.edulearn.auth.dto.AuthResponse;
import com.edulearn.auth.dto.UserResponse;
import com.edulearn.auth.enums.ApprovalStatus;
import com.edulearn.auth.enums.AuthProvider;
import com.edulearn.auth.enums.Role;
import com.edulearn.auth.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Unit Tests")
class AuthControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
    }

    private UserResponse buildUserResponse(Role role) {
        return UserResponse.builder()
                .userId(UUID.randomUUID())
                .fullName("Test User")
                .email("test@example.com")
                .role(role)
                .provider(AuthProvider.LOCAL)
                .approvalStatus(ApprovalStatus.APPROVED)
                .mobile("9876543210")
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ──────────────────────────────────────────────────────────────
    // POST /register
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/v1/auth/register")
    class Register {

        @Test
        @DisplayName("Returns 201 CREATED for student registration")
        void register_student_returns201() throws Exception {
            AuthResponse response = AuthResponse.builder()
                    .email("test@example.com")
                    .message("Registration successful. Please login to continue.")
                    .user(buildUserResponse(Role.STUDENT))
                    .build();

            when(authService.register(any())).thenReturn(response);

            String body = """
                    {
                      "fullName": "Test User",
                      "email": "test@example.com",
                      "password": "Password@123",
                      "role": "STUDENT",
                      "mobile": "9876543210"
                    }
                    """;

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.email").value("test@example.com"))
                    .andExpect(jsonPath("$.message").value("Registration successful. Please login to continue."));
        }

        @Test
        @DisplayName("Returns 201 CREATED for instructor registration with pending message")
        void register_instructor_returns201() throws Exception {
            AuthResponse response = AuthResponse.builder()
                    .email("instructor@example.com")
                    .message("Registration successful. Your instructor account is pending admin approval.")
                    .user(buildUserResponse(Role.INSTRUCTOR))
                    .build();

            when(authService.register(any())).thenReturn(response);

            String body = """
                    {
                      "fullName": "Test Instructor",
                      "email": "instructor@example.com",
                      "password": "Password@123",
                      "role": "INSTRUCTOR",
                      "mobile": "9876543210"
                    }
                    """;

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.message")
                            .value("Registration successful. Your instructor account is pending admin approval."));
        }
    }

    // ──────────────────────────────────────────────────────────────
    // POST /login
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/v1/auth/login")
    class Login {

        @Test
        @DisplayName("Returns 200 OK with token on successful login")
        void login_success_returns200WithToken() throws Exception {
            AuthResponse response = AuthResponse.builder()
                    .token("jwt-token-value")
                    .tokenType("Bearer")
                    .email("test@example.com")
                    .message("Login successful")
                    .user(buildUserResponse(Role.STUDENT))
                    .build();

            when(authService.login(any())).thenReturn(response);

            String body = """
                    {
                      "email": "test@example.com",
                      "password": "Password@123"
                    }
                    """;

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("jwt-token-value"))
                    .andExpect(jsonPath("$.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.message").value("Login successful"));
        }
    }

    // ──────────────────────────────────────────────────────────────
    // GET /me
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/auth/me")
    class GetCurrentUser {

        @Test
        @DisplayName("Returns 200 OK with current user details")
        void getCurrentUser_returns200() throws Exception {
            UserResponse userResponse = buildUserResponse(Role.STUDENT);
            when(authService.getCurrentUser("test@example.com")).thenReturn(userResponse);

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken("test@example.com", null, List.of());

            mockMvc.perform(get("/api/v1/auth/me")
                            .principal(auth))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("test@example.com"))
                    .andExpect(jsonPath("$.fullName").value("Test User"));
        }
    }

    // ──────────────────────────────────────────────────────────────
    // GET /internal/users/{userId}
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/auth/internal/users/{userId}")
    class GetUserById {

        @Test
        @DisplayName("Returns 200 OK with user details for valid UUID")
        void getUserById_returns200() throws Exception {
            String userId = UUID.randomUUID().toString();
            UserResponse userResponse = buildUserResponse(Role.STUDENT);
            when(authService.getUserById(userId)).thenReturn(userResponse);

            mockMvc.perform(get("/api/v1/auth/internal/users/" + userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("test@example.com"));
        }
    }

    // ──────────────────────────────────────────────────────────────
    // GET /ping
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/auth/ping")
    class Ping {

        @Test
        @DisplayName("Returns 200 OK with health check message")
        void ping_returns200() throws Exception {
            mockMvc.perform(get("/api/v1/auth/ping"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Auth service is working"));
        }
    }

    // ──────────────────────────────────────────────────────────────
    // POST /forgot-password
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/v1/auth/forgot-password")
    class ForgotPassword {

        @Test
        @DisplayName("Returns 200 OK with success message")
        void forgotPassword_returns200() throws Exception {
            AuthResponse response = AuthResponse.builder()
                    .email("test@example.com")
                    .message("Password reset link sent to your email")
                    .build();

            when(authService.forgotPassword(any())).thenReturn(response);

            String body = """
                    {
                      "email": "test@example.com"
                    }
                    """;

            mockMvc.perform(post("/api/v1/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Password reset link sent to your email"));
        }
    }

    // ──────────────────────────────────────────────────────────────
    // POST /reset-password
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/v1/auth/reset-password")
    class ResetPassword {

        @Test
        @DisplayName("Returns 200 OK with password reset success message")
        void resetPassword_returns200() throws Exception {
            AuthResponse response = AuthResponse.builder()
                    .email("test@example.com")
                    .message("Password reset successful")
                    .build();

            when(authService.resetPassword(any())).thenReturn(response);

            String body = """
                    {
                      "token": "valid-reset-token",
                      "newPassword": "NewPassword@123"
                    }
                    """;

            mockMvc.perform(post("/api/v1/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Password reset successful"));
        }
    }
}