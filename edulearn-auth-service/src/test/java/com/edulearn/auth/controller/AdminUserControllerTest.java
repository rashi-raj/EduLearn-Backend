package com.edulearn.auth.controller;

import com.edulearn.auth.dto.UserResponse;
import com.edulearn.auth.enums.ApprovalStatus;
import com.edulearn.auth.enums.AuthProvider;
import com.edulearn.auth.enums.Role;
import com.edulearn.auth.service.AdminUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminUserController Unit Tests")
class AdminUserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AdminUserService adminUserService;

    @InjectMocks
    private AdminUserController adminUserController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminUserController).build();
    }

    private UserResponse buildUserResponse(Role role, ApprovalStatus status) {
        return UserResponse.builder()
                .userId(UUID.randomUUID())
                .fullName("Test User")
                .email("test@example.com")
                .role(role)
                .provider(AuthProvider.LOCAL)
                .approvalStatus(status)
                .mobile("9876543210")
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ──────────────────────────────────────────────────────────────
    // GET /pending-instructors
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/admin/users/pending-instructors")
    class GetPendingInstructors {

        @Test
        @DisplayName("Returns 200 OK with list of pending instructors")
        void getPendingInstructors_returns200() throws Exception {
            UserResponse r1 = buildUserResponse(Role.INSTRUCTOR, ApprovalStatus.PENDING);
            when(adminUserService.getPendingInstructors()).thenReturn(List.of(r1));

            mockMvc.perform(get("/api/v1/admin/users/pending-instructors"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].approvalStatus").value("PENDING"));
        }

        @Test
        @DisplayName("Returns 200 OK with empty list when none pending")
        void getPendingInstructors_emptyList() throws Exception {
            when(adminUserService.getPendingInstructors()).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/admin/users/pending-instructors"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // ──────────────────────────────────────────────────────────────
    // GET /approved-instructors
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/admin/users/approved-instructors")
    class GetApprovedInstructors {

        @Test
        @DisplayName("Returns 200 OK with list of approved instructors")
        void getApprovedInstructors_returns200() throws Exception {
            UserResponse r1 = buildUserResponse(Role.INSTRUCTOR, ApprovalStatus.APPROVED);
            when(adminUserService.getApprovedInstructors()).thenReturn(List.of(r1));

            mockMvc.perform(get("/api/v1/admin/users/approved-instructors"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].approvalStatus").value("APPROVED"));
        }
    }

    // ──────────────────────────────────────────────────────────────
    // GET /rejected-instructors
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/admin/users/rejected-instructors")
    class GetRejectedInstructors {

        @Test
        @DisplayName("Returns 200 OK with list of rejected instructors")
        void getRejectedInstructors_returns200() throws Exception {
            UserResponse r1 = buildUserResponse(Role.INSTRUCTOR, ApprovalStatus.REJECTED);
            when(adminUserService.getRejectedInstructors()).thenReturn(List.of(r1));

            mockMvc.perform(get("/api/v1/admin/users/rejected-instructors"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].approvalStatus").value("REJECTED"));
        }
    }

    // ──────────────────────────────────────────────────────────────
    // GET /all
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/admin/users/all")
    class GetAllUsers {

        @Test
        @DisplayName("Returns 200 OK with list of all users")
        void getAllUsers_returns200() throws Exception {
            List<UserResponse> users = List.of(
                    buildUserResponse(Role.STUDENT, ApprovalStatus.APPROVED),
                    buildUserResponse(Role.INSTRUCTOR, ApprovalStatus.PENDING)
            );
            when(adminUserService.getAllUsers()).thenReturn(users);

            mockMvc.perform(get("/api/v1/admin/users/all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        @DisplayName("Returns 200 OK with empty list when no users")
        void getAllUsers_empty() throws Exception {
            when(adminUserService.getAllUsers()).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/admin/users/all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // ──────────────────────────────────────────────────────────────
    // PATCH /{userId}/approve
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("PATCH /api/v1/admin/users/{userId}/approve")
    class ApproveInstructor {

        @Test
        @DisplayName("Returns 200 OK with approved user response")
        void approveInstructor_returns200() throws Exception {
            UUID userId = UUID.randomUUID();
            UserResponse approved = buildUserResponse(Role.INSTRUCTOR, ApprovalStatus.APPROVED);
            when(adminUserService.approveInstructor(userId)).thenReturn(approved);

            mockMvc.perform(patch("/api/v1/admin/users/" + userId + "/approve"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.approvalStatus").value("APPROVED"));
        }
    }

    // ──────────────────────────────────────────────────────────────
    // PATCH /{userId}/reject
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("PATCH /api/v1/admin/users/{userId}/reject")
    class RejectInstructor {

        @Test
        @DisplayName("Returns 200 OK with rejected user response")
        void rejectInstructor_returns200() throws Exception {
            UUID userId = UUID.randomUUID();
            UserResponse rejected = buildUserResponse(Role.INSTRUCTOR, ApprovalStatus.REJECTED);
            when(adminUserService.rejectInstructor(userId)).thenReturn(rejected);

            mockMvc.perform(patch("/api/v1/admin/users/" + userId + "/reject"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.approvalStatus").value("REJECTED"));
        }
    }
}
