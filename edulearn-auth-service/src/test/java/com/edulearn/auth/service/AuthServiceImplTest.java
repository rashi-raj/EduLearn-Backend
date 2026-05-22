package com.edulearn.auth.service;

import com.edulearn.auth.dto.AuthResponse;
import com.edulearn.auth.dto.ForgotPasswordRequest;
import com.edulearn.auth.dto.LoginRequest;
import com.edulearn.auth.dto.RegisterRequest;
import com.edulearn.auth.dto.ResetPasswordRequest;
import com.edulearn.auth.dto.UserResponse;
import com.edulearn.auth.entity.PasswordResetToken;
import com.edulearn.auth.entity.User;
import com.edulearn.auth.enums.ApprovalStatus;
import com.edulearn.auth.enums.AuthProvider;
import com.edulearn.auth.enums.Role;
import com.edulearn.auth.event.NotificationEventPublisher;
import com.edulearn.auth.repository.PasswordResetTokenRepository;
import com.edulearn.auth.repository.UserRepository;
import com.edulearn.auth.security.JwtService;
import com.edulearn.auth.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Unit Tests")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private NotificationEventPublisher notificationEventPublisher;

    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private static final String TEST_SECRET_KEY =
            "bXktc3VwZXItc2VjdXJlLWtleS1mb3ItZWR1bGVhcm4tamF3dC1hdXRoLXNlcnZpY2UtMjAyNg==";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L);

        authService = new AuthServiceImpl(
                userRepository,
                passwordEncoder,
                jwtService,
                authenticationManager,
                passwordResetTokenRepository,
                mailSender,
                notificationEventPublisher
        );

        ReflectionTestUtils.setField(authService, "resetPasswordFrontendUrl",
                "http://localhost:4200/reset-password");
    }

    // ──────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────

    private User buildUser(Role role, ApprovalStatus status) {
        return User.builder()
                .userId(UUID.randomUUID())
                .fullName("Test User")
                .email("test@example.com")
                .passwordHash("encodedPassword")
                .role(role)
                .provider(AuthProvider.LOCAL)
                .approvalStatus(status)
                .mobile("9876543210")
                .bio("Test bio")
                .profilePicUrl("http://pic.url/img.png")
                .createdAt(LocalDateTime.now())
                .build();
    }

    private RegisterRequest buildRegisterRequest(Role role) {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("Test User");
        req.setEmail("test@example.com");
        req.setPassword("Password@123");
        req.setRole(role);
        req.setMobile("9876543210");
        return req;
    }

    // ──────────────────────────────────────────────────────────────
    // register()
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("register()")
    class Register {

        @Test
        @DisplayName("Student registration succeeds with APPROVED status and no token")
        void register_student_success() {
            RegisterRequest req = buildRegisterRequest(Role.STUDENT);
            User saved = buildUser(Role.STUDENT, ApprovalStatus.APPROVED);

            when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
            when(passwordEncoder.encode("Password@123")).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(saved);

            AuthResponse response = authService.register(req);

            assertNotNull(response);
            assertNull(response.getToken(), "Token must be null on register");
            assertEquals("test@example.com", response.getEmail());
            assertEquals("Registration successful. Please login to continue.", response.getMessage());
            assertNotNull(response.getUser());
            assertEquals(Role.STUDENT, response.getUser().getRole());
            verify(notificationEventPublisher, times(1)).publish(any());
            verify(userRepository, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("Instructor registration succeeds with PENDING status and pending message")
        void register_instructor_success_pendingStatus() {
            RegisterRequest req = buildRegisterRequest(Role.INSTRUCTOR);
            User saved = buildUser(Role.INSTRUCTOR, ApprovalStatus.PENDING);

            when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
            when(passwordEncoder.encode("Password@123")).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(saved);

            AuthResponse response = authService.register(req);

            assertNotNull(response);
            assertEquals("Registration successful. Your instructor account is pending admin approval.",
                    response.getMessage());
            assertEquals(ApprovalStatus.PENDING, response.getUser().getApprovalStatus());
        }

        @Test
        @DisplayName("register() throws RuntimeException when email is already registered")
        void register_duplicateEmail_throwsException() {
            RegisterRequest req = buildRegisterRequest(Role.STUDENT);
            when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

            RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.register(req));
            assertEquals("Email already registered", ex.getMessage());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Notification event is published with correct fields on successful registration")
        void register_publishesNotificationEvent() {
            RegisterRequest req = buildRegisterRequest(Role.STUDENT);
            User saved = buildUser(Role.STUDENT, ApprovalStatus.APPROVED);

            when(userRepository.existsByEmail(any())).thenReturn(false);
            when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
            when(userRepository.save(any())).thenReturn(saved);

            authService.register(req);

            verify(notificationEventPublisher, times(1)).publish(argThat(event ->
                    "USER_REGISTERED".equals(event.getEventType()) &&
                    event.getUserId().equals(saved.getUserId().toString())
            ));
        }

        @Test
        @DisplayName("Admin registration succeeds with APPROVED status")
        void register_admin_success() {
            RegisterRequest req = buildRegisterRequest(Role.ADMIN);
            User saved = buildUser(Role.ADMIN, ApprovalStatus.APPROVED);

            when(userRepository.existsByEmail(any())).thenReturn(false);
            when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
            when(userRepository.save(any())).thenReturn(saved);

            AuthResponse response = authService.register(req);

            assertEquals(ApprovalStatus.APPROVED, response.getUser().getApprovalStatus());
        }
    }

    // ──────────────────────────────────────────────────────────────
    // login()
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("Student login succeeds and returns JWT token")
        void login_student_success() {
            LoginRequest req = new LoginRequest();
            req.setEmail("test@example.com");
            req.setPassword("Password@123");

            User user = buildUser(Role.STUDENT, ApprovalStatus.APPROVED);

            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("Password@123", "encodedPassword")).thenReturn(true);

            AuthResponse response = authService.login(req);

            assertNotNull(response);
            assertNotNull(response.getToken());
            assertEquals("Bearer", response.getTokenType());
            assertEquals("Login successful", response.getMessage());
        }

        @Test
        @DisplayName("Approved instructor login succeeds")
        void login_approvedInstructor_success() {
            LoginRequest req = new LoginRequest();
            req.setEmail("test@example.com");
            req.setPassword("Password@123");

            User user = buildUser(Role.INSTRUCTOR, ApprovalStatus.APPROVED);

            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("Password@123", "encodedPassword")).thenReturn(true);

            AuthResponse response = authService.login(req);

            assertNotNull(response.getToken());
        }

        @Test
        @DisplayName("login() throws for pending instructor")
        void login_pendingInstructor_throws() {
            LoginRequest req = new LoginRequest();
            req.setEmail("test@example.com");
            req.setPassword("Password@123");

            User user = buildUser(Role.INSTRUCTOR, ApprovalStatus.PENDING);

            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("Password@123", "encodedPassword")).thenReturn(true);

            RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(req));
            assertEquals("Your instructor account is pending admin approval.", ex.getMessage());
        }

        @Test
        @DisplayName("login() throws for rejected instructor")
        void login_rejectedInstructor_throws() {
            LoginRequest req = new LoginRequest();
            req.setEmail("test@example.com");
            req.setPassword("Password@123");

            User user = buildUser(Role.INSTRUCTOR, ApprovalStatus.REJECTED);

            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("Password@123", "encodedPassword")).thenReturn(true);

            RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(req));
            assertEquals("Your instructor account has been rejected by admin.", ex.getMessage());
        }

        @Test
        @DisplayName("login() throws for wrong password")
        void login_wrongPassword_throws() {
            LoginRequest req = new LoginRequest();
            req.setEmail("test@example.com");
            req.setPassword("WrongPassword");

            User user = buildUser(Role.STUDENT, ApprovalStatus.APPROVED);

            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("WrongPassword", "encodedPassword")).thenReturn(false);

            RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(req));
            assertEquals("Invalid email or password", ex.getMessage());
        }

        @Test
        @DisplayName("login() throws when email not found")
        void login_emailNotFound_throws() {
            LoginRequest req = new LoginRequest();
            req.setEmail("ghost@example.com");
            req.setPassword("Password@123");

            when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(req));
            assertEquals("Invalid email or password", ex.getMessage());
        }

        @Test
        @DisplayName("login() returns correct user details in response")
        void login_returnsCorrectUserDetails() {
            LoginRequest req = new LoginRequest();
            req.setEmail("test@example.com");
            req.setPassword("Password@123");

            User user = buildUser(Role.STUDENT, ApprovalStatus.APPROVED);

            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("Password@123", "encodedPassword")).thenReturn(true);

            AuthResponse response = authService.login(req);

            assertNotNull(response.getUser());
            assertEquals(user.getEmail(), response.getUser().getEmail());
            assertEquals(user.getFullName(), response.getUser().getFullName());
            assertEquals(Role.STUDENT, response.getUser().getRole());
        }
    }

    // ──────────────────────────────────────────────────────────────
    // getCurrentUser()
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getCurrentUser()")
    class GetCurrentUser {

        @Test
        @DisplayName("Returns user response for valid email")
        void getCurrentUser_found() {
            User user = buildUser(Role.STUDENT, ApprovalStatus.APPROVED);
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

            UserResponse response = authService.getCurrentUser("test@example.com");

            assertNotNull(response);
            assertEquals("test@example.com", response.getEmail());
            assertEquals("Test User", response.getFullName());
            assertEquals("Test bio", response.getBio());
            assertEquals("http://pic.url/img.png", response.getProfilePicUrl());
            assertEquals("9876543210", response.getMobile());
        }

        @Test
        @DisplayName("Throws RuntimeException when user not found by email")
        void getCurrentUser_notFound_throws() {
            when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> authService.getCurrentUser("ghost@example.com"));
            assertEquals("User not found", ex.getMessage());
        }
    }

    // ──────────────────────────────────────────────────────────────
    // getUserById()
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getUserById()")
    class GetUserById {

        @Test
        @DisplayName("Returns user response for valid UUID")
        void getUserById_found() {
            UUID userId = UUID.randomUUID();
            User user = User.builder()
                    .userId(userId)
                    .fullName("Test User")
                    .email("test@example.com")
                    .passwordHash("encodedPassword")
                    .role(Role.STUDENT)
                    .provider(AuthProvider.LOCAL)
                    .approvalStatus(ApprovalStatus.APPROVED)
                    .mobile("9876543210")
                    .createdAt(LocalDateTime.now())
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            UserResponse response = authService.getUserById(userId.toString());

            assertNotNull(response);
            assertEquals(userId, response.getUserId());
            assertEquals("test@example.com", response.getEmail());
        }

        @Test
        @DisplayName("Throws RuntimeException when user UUID not found")
        void getUserById_notFound_throws() {
            UUID userId = UUID.randomUUID();
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> authService.getUserById(userId.toString()));
            assertEquals("User not found", ex.getMessage());
        }
    }

    // ──────────────────────────────────────────────────────────────
    // forgotPassword()
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("forgotPassword()")
    class ForgotPassword {

        @Test
        @DisplayName("Sends reset email and returns success response")
        void forgotPassword_success() {
            ForgotPasswordRequest req = new ForgotPasswordRequest();
            req.setEmail("test@example.com");

            User user = buildUser(Role.STUDENT, ApprovalStatus.APPROVED);
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(passwordResetTokenRepository.save(any(PasswordResetToken.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            doNothing().when(mailSender).send(any(SimpleMailMessage.class));

            AuthResponse response = authService.forgotPassword(req);

            assertNotNull(response);
            assertEquals("test@example.com", response.getEmail());
            assertEquals("Password reset link sent to your email", response.getMessage());
            verify(passwordResetTokenRepository, times(1)).save(any(PasswordResetToken.class));
            verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
        }

        @Test
        @DisplayName("Throws RuntimeException when email not found")
        void forgotPassword_emailNotFound_throws() {
            ForgotPasswordRequest req = new ForgotPasswordRequest();
            req.setEmail("ghost@example.com");

            when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> authService.forgotPassword(req));
            assertEquals("User not found with this email", ex.getMessage());
        }

        @Test
        @DisplayName("Still returns success response even when SMTP mail fails")
        void forgotPassword_mailException_stillSucceeds() {
            ForgotPasswordRequest req = new ForgotPasswordRequest();
            req.setEmail("test@example.com");

            User user = buildUser(Role.STUDENT, ApprovalStatus.APPROVED);
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(passwordResetTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            doThrow(new MailSendException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));

            AuthResponse response = authService.forgotPassword(req);

            assertNotNull(response);
            assertEquals("Password reset link sent to your email", response.getMessage());
        }

        @Test
        @DisplayName("Reset token is saved with correct email and not-used flag")
        void forgotPassword_tokenSavedCorrectly() {
            ForgotPasswordRequest req = new ForgotPasswordRequest();
            req.setEmail("test@example.com");

            User user = buildUser(Role.STUDENT, ApprovalStatus.APPROVED);
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(passwordResetTokenRepository.save(any(PasswordResetToken.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            doNothing().when(mailSender).send(any(SimpleMailMessage.class));

            authService.forgotPassword(req);

            verify(passwordResetTokenRepository).save(argThat(token ->
                    !token.isUsed() &&
                    "test@example.com".equals(token.getEmail()) &&
                    token.getToken() != null &&
                    token.getExpiryDate().isAfter(LocalDateTime.now())
            ));
        }
    }

    // ──────────────────────────────────────────────────────────────
    // resetPassword()
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("resetPassword()")
    class ResetPassword {

        @Test
        @DisplayName("Resets password successfully for valid token")
        void resetPassword_success() {
            ResetPasswordRequest req = new ResetPasswordRequest();
            req.setToken("valid-token");
            req.setNewPassword("NewPassword@123");

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token("valid-token")
                    .email("test@example.com")
                    .expiryDate(LocalDateTime.now().plusMinutes(10))
                    .used(false)
                    .build();

            User user = buildUser(Role.STUDENT, ApprovalStatus.APPROVED);

            when(passwordResetTokenRepository.findByToken("valid-token"))
                    .thenReturn(Optional.of(resetToken));
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.encode("NewPassword@123")).thenReturn("newEncodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(passwordResetTokenRepository.save(any(PasswordResetToken.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            AuthResponse response = authService.resetPassword(req);

            assertNotNull(response);
            assertEquals("test@example.com", response.getEmail());
            assertEquals("Password reset successful", response.getMessage());
            assertTrue(resetToken.isUsed(), "Reset token must be marked as used");
            verify(userRepository, times(1)).save(any(User.class));
            verify(passwordResetTokenRepository, times(1)).save(resetToken);
        }

        @Test
        @DisplayName("Throws RuntimeException for invalid/non-existent token")
        void resetPassword_invalidToken_throws() {
            ResetPasswordRequest req = new ResetPasswordRequest();
            req.setToken("bad-token");
            req.setNewPassword("NewPassword@123");

            when(passwordResetTokenRepository.findByToken("bad-token")).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> authService.resetPassword(req));
            assertEquals("Invalid reset token", ex.getMessage());
        }

        @Test
        @DisplayName("Throws RuntimeException when token has already been used")
        void resetPassword_alreadyUsedToken_throws() {
            ResetPasswordRequest req = new ResetPasswordRequest();
            req.setToken("used-token");
            req.setNewPassword("NewPassword@123");

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token("used-token")
                    .email("test@example.com")
                    .expiryDate(LocalDateTime.now().plusMinutes(10))
                    .used(true)
                    .build();

            when(passwordResetTokenRepository.findByToken("used-token"))
                    .thenReturn(Optional.of(resetToken));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> authService.resetPassword(req));
            assertEquals("Reset token has already been used", ex.getMessage());
        }

        @Test
        @DisplayName("Throws RuntimeException when token has expired")
        void resetPassword_expiredToken_throws() {
            ResetPasswordRequest req = new ResetPasswordRequest();
            req.setToken("expired-token");
            req.setNewPassword("NewPassword@123");

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token("expired-token")
                    .email("test@example.com")
                    .expiryDate(LocalDateTime.now().minusMinutes(5)) // already expired
                    .used(false)
                    .build();

            when(passwordResetTokenRepository.findByToken("expired-token"))
                    .thenReturn(Optional.of(resetToken));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> authService.resetPassword(req));
            assertEquals("Reset token has expired", ex.getMessage());
        }

        @Test
        @DisplayName("Throws RuntimeException when user not found during password reset")
        void resetPassword_userNotFound_throws() {
            ResetPasswordRequest req = new ResetPasswordRequest();
            req.setToken("valid-token");
            req.setNewPassword("NewPassword@123");

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token("valid-token")
                    .email("ghost@example.com")
                    .expiryDate(LocalDateTime.now().plusMinutes(10))
                    .used(false)
                    .build();

            when(passwordResetTokenRepository.findByToken("valid-token"))
                    .thenReturn(Optional.of(resetToken));
            when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> authService.resetPassword(req));
            assertEquals("User not found", ex.getMessage());
        }

        @Test
        @DisplayName("Password encoder is called with the new password during reset")
        void resetPassword_encodesNewPassword() {
            ResetPasswordRequest req = new ResetPasswordRequest();
            req.setToken("valid-token");
            req.setNewPassword("NewPassword@123");

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token("valid-token")
                    .email("test@example.com")
                    .expiryDate(LocalDateTime.now().plusMinutes(10))
                    .used(false)
                    .build();

            User user = buildUser(Role.STUDENT, ApprovalStatus.APPROVED);

            when(passwordResetTokenRepository.findByToken("valid-token"))
                    .thenReturn(Optional.of(resetToken));
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.encode("NewPassword@123")).thenReturn("newEncodedPassword");
            when(userRepository.save(any())).thenReturn(user);
            when(passwordResetTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            authService.resetPassword(req);

            verify(passwordEncoder, times(1)).encode("NewPassword@123");
            assertEquals("newEncodedPassword", user.getPasswordHash());
        }
    }
}