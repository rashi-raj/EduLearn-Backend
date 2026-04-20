package com.edulearn.auth.service.impl;

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
import com.edulearn.auth.event.NotificationEvent;
import com.edulearn.auth.event.NotificationEventPublisher;
import com.edulearn.auth.repository.PasswordResetTokenRepository;
import com.edulearn.auth.repository.UserRepository;
import com.edulearn.auth.security.JwtService;
import com.edulearn.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JavaMailSender mailSender;
    private final NotificationEventPublisher notificationEventPublisher;

    @Value("${application.reset-password.frontend-url}")
    private String resetPasswordFrontendUrl;

    @Override
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        ApprovalStatus approvalStatus =
                request.getRole() == Role.INSTRUCTOR
                        ? ApprovalStatus.PENDING
                        : ApprovalStatus.APPROVED;

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .provider(AuthProvider.LOCAL)
                .approvalStatus(approvalStatus)
                .mobile(request.getMobile())
                .build();

        User savedUser = userRepository.save(user);

        notificationEventPublisher.publish(NotificationEvent.builder()
                .eventType("USER_REGISTERED")
                .userId(savedUser.getUserId().toString())
                .title("Welcome to EduLearn!")
                .message("Your account has been created successfully. Start exploring courses now!")
                .build());

        return AuthResponse.builder()
                .token(null) // do not auto-login on register
                .tokenType("Bearer")
                .user(UserResponse.builder()
                        .userId(savedUser.getUserId())
                        .fullName(savedUser.getFullName())
                        .email(savedUser.getEmail())
                        .role(savedUser.getRole())
                        .provider(savedUser.getProvider())
                        .approvalStatus(savedUser.getApprovalStatus())
                        .mobile(savedUser.getMobile())
                        .bio(savedUser.getBio())
                        .profilePicUrl(savedUser.getProfilePicUrl())
                        .createdAt(savedUser.getCreatedAt())
                        .build())
                .email(savedUser.getEmail())
                .message(
                        savedUser.getRole() == Role.INSTRUCTOR
                                ? "Registration successful. Your instructor account is pending admin approval."
                                : "Registration successful. Please login to continue."
                )
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        if (user.getRole() == Role.INSTRUCTOR &&
            user.getApprovalStatus() != ApprovalStatus.APPROVED) {

            String message = user.getApprovalStatus() == ApprovalStatus.REJECTED
                    ? "Your instructor account has been rejected by admin."
                    : "Your instructor account is pending admin approval.";

            throw new RuntimeException(message);
        }

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );

        String token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(mapToUserResponse(user))
                .email(user.getEmail())
                .message("Login successful")
                .build();
    }

    @Override
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return mapToUserResponse(user);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .provider(user.getProvider())
                .approvalStatus(user.getApprovalStatus())
                .mobile(user.getMobile())
                .bio(user.getBio())
                .profilePicUrl(user.getProfilePicUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Override
    public AuthResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with this email"));

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .email(user.getEmail())
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build();

        passwordResetTokenRepository.save(resetToken);

        String resetLink = resetPasswordFrontendUrl + "?token=" + token;

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(user.getEmail());
        mailMessage.setSubject("EduLearn Password Reset");
        mailMessage.setText(
                "Hello " + user.getFullName() + ",\n\n"
                        + "Click the link below to reset your password:\n"
                        + resetLink + "\n\n"
                        + "This link will expire in 15 minutes."
        );

        mailSender.send(mailMessage);

        return AuthResponse.builder()
                .email(user.getEmail())
                .message("Password reset link sent to your email")
                .build();
    }

    @Override
    public AuthResponse resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));

        if (resetToken.isUsed()) {
            throw new RuntimeException("Reset token has already been used");
        }

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token has expired");
        }

        User user = userRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        return AuthResponse.builder()
                .email(user.getEmail())
                .message("Password reset successful")
                .build();
    }
}