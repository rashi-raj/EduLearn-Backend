package com.edulearn.enrollment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "enrollments",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"studentId", "courseId"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID enrollmentId;

    @Column(nullable = false)
    private UUID studentId;

    @Column(nullable = false)
    private UUID courseId;

    @Column(nullable = false)
    private LocalDateTime enrolledAt;
    
    private LocalDateTime expiresAt;

    private Boolean accessExpired;

    @Column
    private LocalDateTime completedAt;

    @Column(nullable = false, length = 20)
    private String status; // ACTIVE, COMPLETED, CANCELLED

    @Column(nullable = false)
    private Double progressPercent;

    @Column(nullable = false)
    private Boolean certificateIssued;

    @Column(nullable = false)
    private String studentName;

    @Column(nullable = false)
    private String studentEmail;

}