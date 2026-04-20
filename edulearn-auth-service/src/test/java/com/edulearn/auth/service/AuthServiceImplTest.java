package com.edulearn.auth.service;

import com.edulearn.auth.dto.LoginRequest;
import com.edulearn.auth.dto.RegisterRequest;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(jwtService, "secretKey",
                "bXktc3VwZXItc2VjdXJlLWtleS1mb3ItZWR1bGVhcm4tamF3dC1hdXRoLXNlcnZpY2UtMjAyNg==");
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

    @Test
    void register_shouldCreateStudentSuccessfully() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Rashi Raj");
        request.setEmail("rashi@test.com");
        request.setPassword("Password@123");
        request.setRole(Role.STUDENT);
        request.setMobile("9876543210");

        when(userRepository.existsByEmail("rashi@test.com")).thenReturn(false);
        when(passwordEncoder.encode("Password@123")).thenReturn("encodedPassword");

        User savedUser = User.builder()
                .userId(UUID.randomUUID())
                .fullName("Rashi Raj")
                .email("rashi@test.com")
                .passwordHash("encodedPassword")
                .role(Role.STUDENT)
                .provider(AuthProvider.LOCAL)
                .approvalStatus(ApprovalStatus.APPROVED)
                .mobile("9876543210")
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        var response = authService.register(request);

        assertNotNull(response);
        assertEquals("rashi@test.com", response.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void login_shouldReturnTokenForApprovedUser() {
        LoginRequest request = new LoginRequest();
        request.setEmail("rashi@test.com");
        request.setPassword("Password@123");

        User user = User.builder()
                .userId(UUID.randomUUID())
                .fullName("Rashi Raj")
                .email("rashi@test.com")
                .passwordHash("encodedPassword")
                .role(Role.STUDENT)
                .provider(AuthProvider.LOCAL)
                .approvalStatus(ApprovalStatus.APPROVED)
                .build();

        when(userRepository.findByEmail("rashi@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password@123", "encodedPassword")).thenReturn(true);

        var response = authService.login(request);

        assertNotNull(response);
        assertNotNull(response.getToken());
    }

    @Test
    void login_shouldFailForInvalidPassword() {
        LoginRequest request = new LoginRequest();
        request.setEmail("rashi@test.com");
        request.setPassword("wrong-password");

        User user = User.builder()
                .email("rashi@test.com")
                .passwordHash("encodedPassword")
                .role(Role.STUDENT)
                .approvalStatus(ApprovalStatus.APPROVED)
                .build();

        when(userRepository.findByEmail("rashi@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encodedPassword")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(request));
        assertEquals("Invalid email or password", ex.getMessage());
    }
}