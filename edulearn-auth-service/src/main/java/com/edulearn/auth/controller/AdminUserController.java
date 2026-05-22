package com.edulearn.auth.controller;

import com.edulearn.auth.dto.UserResponse;
import com.edulearn.auth.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Admin User Controller", description = "Admin APIs for managing instructors and users")
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "Get pending instructors")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/pending-instructors")
    public ResponseEntity<List<UserResponse>> getPendingInstructors() {
        return ResponseEntity.ok(adminUserService.getPendingInstructors());
    }

    @Operation(summary = "Get approved instructors")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/approved-instructors")
    public ResponseEntity<List<UserResponse>> getApprovedInstructors() {
        return ResponseEntity.ok(adminUserService.getApprovedInstructors());
    }

    @Operation(summary = "Get rejected instructors")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/rejected-instructors")
    public ResponseEntity<List<UserResponse>> getRejectedInstructors() {
        return ResponseEntity.ok(adminUserService.getRejectedInstructors());
    }

    @Operation(summary = "Get all users")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminUserService.getAllUsers());
    }

    @Operation(summary = "Approve instructor")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PatchMapping("/{userId}/approve")
    public ResponseEntity<UserResponse> approveInstructor(@PathVariable UUID userId) {
        return ResponseEntity.ok(adminUserService.approveInstructor(userId));
    }

    @Operation(summary = "Reject instructor")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PatchMapping("/{userId}/reject")
    public ResponseEntity<UserResponse> rejectInstructor(@PathVariable UUID userId) {
        return ResponseEntity.ok(adminUserService.rejectInstructor(userId));
    }
}