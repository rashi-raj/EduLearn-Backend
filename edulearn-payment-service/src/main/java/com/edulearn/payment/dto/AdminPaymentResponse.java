package com.edulearn.payment.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

import com.edulearn.payment.entity.PaymentStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminPaymentResponse {
    private UUID paymentId;
    private UUID studentId;
    private String studentName;
    private String studentEmail;
    private UUID courseId;
    private String courseTitle;
    private Double amount;
    private String currency;
    private String paymentMethod;
    private String paymentStatus;
    private String receipt;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
}