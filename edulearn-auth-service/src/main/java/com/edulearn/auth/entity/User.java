package com.edulearn.auth.entity;

import com.edulearn.auth.enums.ApprovalStatus;
import com.edulearn.auth.enums.AuthProvider;
import com.edulearn.auth.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @Column(name = "user_id", columnDefinition = "BINARY(16)")
    private UUID userId;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStatus approvalStatus;

    private String mobile;

    @Column(length = 1000)
    private String bio;

    private String profilePicUrl;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (userId == null) {
            userId = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}