package com.edulearn.auth;

import com.edulearn.auth.dto.*;
import com.edulearn.auth.entity.PasswordResetToken;
import com.edulearn.auth.entity.User;
import com.edulearn.auth.enums.ApprovalStatus;
import com.edulearn.auth.enums.AuthProvider;
import com.edulearn.auth.enums.Role;
import com.edulearn.auth.event.NotificationEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Lombok-generated Methods Coverage Test")
class LombokTest {

    @Test
    @DisplayName("Verify DTOs and Entities coverage")
    void testDataClasses() {
        // User Entity
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setEmail("test@test.com");
        user.setFullName("Test User");
        user.setPasswordHash("hash");
        user.setRole(Role.STUDENT);
        user.setProvider(AuthProvider.LOCAL);
        user.setApprovalStatus(ApprovalStatus.APPROVED);
        user.setCreatedAt(LocalDateTime.now());
        
        assertNotNull(user.getUserId());
        assertEquals("test@test.com", user.getEmail());
        assertEquals("Test User", user.getFullName());
        assertEquals("hash", user.getPasswordHash());
        assertEquals(Role.STUDENT, user.getRole());
        assertEquals(AuthProvider.LOCAL, user.getProvider());
        assertEquals(ApprovalStatus.APPROVED, user.getApprovalStatus());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.toString());
        
        User user2 = User.builder().email("test@test.com").build();
        assertEquals("test@test.com", user2.getEmail());

        // PasswordResetToken
        PasswordResetToken token = new PasswordResetToken();
        token.setId(UUID.randomUUID());
        token.setToken("token-123");
        token.setEmail("test@test.com");
        token.setExpiryDate(LocalDateTime.now().plusHours(1));
        token.setUsed(false);
        
        assertNotNull(token.getId());
        assertEquals("token-123", token.getToken());
        assertEquals("test@test.com", token.getEmail());
        assertNotNull(token.getExpiryDate());
        assertFalse(token.isUsed());

        // DTOs
        AuthResponse authResponse = new AuthResponse("token", "Bearer", null, "email", "msg");
        assertEquals("token", authResponse.getToken());
        
        ForgotPasswordRequest forgotRequest = new ForgotPasswordRequest();
        forgotRequest.setEmail("email");
        assertEquals("email", forgotRequest.getEmail());
        
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("user@test.com");
        loginRequest.setPassword("pass123");
        assertEquals("user@test.com", loginRequest.getEmail());
        assertEquals("pass123", loginRequest.getPassword());
        
        RegisterRequest regRequest = new RegisterRequest();
        regRequest.setEmail("email");
        regRequest.setFullName("name");
        regRequest.setPassword("pass");
        regRequest.setRole(Role.STUDENT);
        assertEquals(Role.STUDENT, regRequest.getRole());
        
        ResetPasswordRequest resetRequest = new ResetPasswordRequest();
        resetRequest.setToken("t");
        resetRequest.setNewPassword("p");
        assertEquals("t", resetRequest.getToken());
        
        UserResponse userResponse = UserResponse.builder()
                .userId(UUID.randomUUID())
                .fullName("name")
                .email("email")
                .role(Role.STUDENT)
                .provider(AuthProvider.LOCAL)
                .approvalStatus(ApprovalStatus.APPROVED)
                .profilePicUrl("pic")
                .createdAt(LocalDateTime.now())
                .build();
        assertNotNull(userResponse.getUserId());

        // Events
        NotificationEvent event = NotificationEvent.builder()
                .userId("u")
                .eventType("t")
                .title("T")
                .message("M")
                .build();
        assertEquals("u", event.getUserId());
        assertNotNull(event.toString());
    }
}
