package com.edulearn.payment.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class PaymentResponse {
    private UUID paymentId;
    private UUID courseId;
    private UUID studentId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String paymentStatus;
    private String receipt;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
}