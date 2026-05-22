package com.edulearn.auth.service;

import com.edulearn.auth.dto.UserResponse;
import com.edulearn.auth.entity.User;
import com.edulearn.auth.enums.ApprovalStatus;
import com.edulearn.auth.enums.AuthProvider;
import com.edulearn.auth.enums.Role;
import com.edulearn.auth.repository.UserRepository;
import com.edulearn.auth.service.impl.AdminUserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminUserServiceImpl Unit Tests")
class AdminUserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminUserServiceImpl adminUserService;

    private User buildUser(UUID id, Role role, ApprovalStatus status) {
        return User.builder()
                .userId(id)
                .fullName("Test User")
                .email("test@example.com")
                .passwordHash("encodedPassword")
                .role(role)
                .provider(AuthProvider.LOCAL)
                .approvalStatus(status)
                .mobile("9876543210")
                .bio("bio")
                .profilePicUrl("http://img.url")
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ──────────────────────────────────────────────────────────────
    // getPendingInstructors()
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getPendingInstructors()")
    class GetPendingInstructors {

        @Test
        @DisplayName("Returns list of pending instructors")
        void getPendingInstructors_returnsList() {
            User u1 = buildUser(UUID.randomUUID(), Role.INSTRUCTOR, ApprovalStatus.PENDING);
            User u2 = buildUser(UUID.randomUUID(), Role.INSTRUCTOR, ApprovalStatus.PENDING);

            when(userRepository.findAllByRoleAndApprovalStatus(Role.INSTRUCTOR, ApprovalStatus.PENDING))
                    .thenReturn(List.of(u1, u2));

            List<UserResponse> result = adminUserService.getPendingInstructors();

            assertEquals(2, result.size());
            result.forEach(r -> assertEquals(ApprovalStatus.PENDING, r.getApprovalStatus()));
        }

        @Test
        @DisplayName("Returns empty list when no pending instructors")
        void getPendingInstructors_empty() {
            when(userRepository.findAllByRoleAndApprovalStatus(Role.INSTRUCTOR, ApprovalStatus.PENDING))
                    .thenReturn(List.of());

            List<UserResponse> result = adminUserService.getPendingInstructors();
            assertTrue(result.isEmpty());
        }
    }

    // ──────────────────────────────────────────────────────────────
    // getApprovedInstructors()
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getApprovedInstructors()")
    class GetApprovedInstructors {

        @Test
        @DisplayName("Returns list of approved instructors")
        void getApprovedInstructors_returnsList() {
            User u1 = buildUser(UUID.randomUUID(), Role.INSTRUCTOR, ApprovalStatus.APPROVED);

            when(userRepository.findAllByRoleAndApprovalStatus(Role.INSTRUCTOR, ApprovalStatus.APPROVED))
                    .thenReturn(List.of(u1));

            List<UserResponse> result = adminUserService.getApprovedInstructors();

            assertEquals(1, result.size());
            assertEquals(ApprovalStatus.APPROVED, result.get(0).getApprovalStatus());
        }

        @Test
        @DisplayName("Returns empty list when no approved instructors")
        void getApprovedInstructors_empty() {
            when(userRepository.findAllByRoleAndApprovalStatus(Role.INSTRUCTOR, ApprovalStatus.APPROVED))
                    .thenReturn(List.of());

            assertTrue(adminUserService.getApprovedInstructors().isEmpty());
        }
    }

    // ──────────────────────────────────────────────────────────────
    // getRejectedInstructors()
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getRejectedInstructors()")
    class GetRejectedInstructors {

        @Test
        @DisplayName("Returns list of rejected instructors")
        void getRejectedInstructors_returnsList() {
            User u1 = buildUser(UUID.randomUUID(), Role.INSTRUCTOR, ApprovalStatus.REJECTED);

            when(userRepository.findAllByRoleAndApprovalStatus(Role.INSTRUCTOR, ApprovalStatus.REJECTED))
                    .thenReturn(List.of(u1));

            List<UserResponse> result = adminUserService.getRejectedInstructors();

            assertEquals(1, result.size());
            assertEquals(ApprovalStatus.REJECTED, result.get(0).getApprovalStatus());
        }

        @Test
        @DisplayName("Returns empty list when no rejected instructors")
        void getRejectedInstructors_empty() {
            when(userRepository.findAllByRoleAndApprovalStatus(Role.INSTRUCTOR, ApprovalStatus.REJECTED))
                    .thenReturn(List.of());

            assertTrue(adminUserService.getRejectedInstructors().isEmpty());
        }
    }

    // ──────────────────────────────────────────────────────────────
    // getAllUsers()
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getAllUsers()")
    class GetAllUsers {

        @Test
        @DisplayName("Returns all users from repository")
        void getAllUsers_returnsList() {
            User u1 = buildUser(UUID.randomUUID(), Role.STUDENT, ApprovalStatus.APPROVED);
            User u2 = buildUser(UUID.randomUUID(), Role.INSTRUCTOR, ApprovalStatus.PENDING);
            User u3 = buildUser(UUID.randomUUID(), Role.ADMIN, ApprovalStatus.APPROVED);

            when(userRepository.findAll()).thenReturn(List.of(u1, u2, u3));

            List<UserResponse> result = adminUserService.getAllUsers();
            assertEquals(3, result.size());
        }

        @Test
        @DisplayName("Returns empty list when no users exist")
        void getAllUsers_empty() {
            when(userRepository.findAll()).thenReturn(List.of());
            assertTrue(adminUserService.getAllUsers().isEmpty());
        }

        @Test
        @DisplayName("Correctly maps all user fields in response")
        void getAllUsers_fieldMapping() {
            UUID id = UUID.randomUUID();
            User user = buildUser(id, Role.STUDENT, ApprovalStatus.APPROVED);

            when(userRepository.findAll()).thenReturn(List.of(user));

            List<UserResponse> result = adminUserService.getAllUsers();
            UserResponse response = result.get(0);

            assertEquals(id, response.getUserId());
            assertEquals("Test User", response.getFullName());
            assertEquals("test@example.com", response.getEmail());
            assertEquals(Role.STUDENT, response.getRole());
            assertEquals(AuthProvider.LOCAL, response.getProvider());
            assertEquals(ApprovalStatus.APPROVED, response.getApprovalStatus());
            assertEquals("9876543210", response.getMobile());
            assertEquals("bio", response.getBio());
            assertEquals("http://img.url", response.getProfilePicUrl());
        }
    }

    // ──────────────────────────────────────────────────────────────
    // approveInstructor()
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("approveInstructor()")
    class ApproveInstructor {

        @Test
        @DisplayName("Approves a pending instructor successfully")
        void approveInstructor_success() {
            UUID userId = UUID.randomUUID();
            User user = buildUser(userId, Role.INSTRUCTOR, ApprovalStatus.PENDING);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            UserResponse response = adminUserService.approveInstructor(userId);

            assertEquals(ApprovalStatus.APPROVED, response.getApprovalStatus());
            assertEquals(userId, response.getUserId());
            verify(userRepository, times(1)).save(user);
        }

        @Test
        @DisplayName("Throws RuntimeException when instructor not found")
        void approveInstructor_notFound_throws() {
            UUID userId = UUID.randomUUID();
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> adminUserService.approveInstructor(userId));
            assertEquals("User not found", ex.getMessage());
        }

        @Test
        @DisplayName("Throws RuntimeException when approving a non-instructor user")
        void approveInstructor_nonInstructor_throws() {
            UUID userId = UUID.randomUUID();
            User student = buildUser(userId, Role.STUDENT, ApprovalStatus.APPROVED);

            when(userRepository.findById(userId)).thenReturn(Optional.of(student));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> adminUserService.approveInstructor(userId));
            assertEquals("Only instructor accounts can be approved.", ex.getMessage());
            verify(userRepository, never()).save(any());
        }
    }

    // ──────────────────────────────────────────────────────────────
    // rejectInstructor()
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("rejectInstructor()")
    class RejectInstructor {

        @Test
        @DisplayName("Rejects a pending instructor successfully")
        void rejectInstructor_success() {
            UUID userId = UUID.randomUUID();
            User user = buildUser(userId, Role.INSTRUCTOR, ApprovalStatus.PENDING);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            UserResponse response = adminUserService.rejectInstructor(userId);

            assertEquals(ApprovalStatus.REJECTED, response.getApprovalStatus());
            verify(userRepository, times(1)).save(user);
        }

        @Test
        @DisplayName("Throws RuntimeException when instructor not found")
        void rejectInstructor_notFound_throws() {
            UUID userId = UUID.randomUUID();
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> adminUserService.rejectInstructor(userId));
            assertEquals("User not found", ex.getMessage());
        }

        @Test
        @DisplayName("Throws RuntimeException when rejecting a non-instructor user")
        void rejectInstructor_nonInstructor_throws() {
            UUID userId = UUID.randomUUID();
            User admin = buildUser(userId, Role.ADMIN, ApprovalStatus.APPROVED);

            when(userRepository.findById(userId)).thenReturn(Optional.of(admin));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> adminUserService.rejectInstructor(userId));
            assertEquals("Only instructor accounts can be rejected.", ex.getMessage());
            verify(userRepository, never()).save(any());
        }
    }
}
