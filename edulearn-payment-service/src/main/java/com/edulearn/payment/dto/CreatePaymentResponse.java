package com.edulearn.payment.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class CreatePaymentResponse {
    private UUID paymentId;
    private UUID courseId;
    private UUID studentId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String paymentStatus;
    private String razorpayOrderId;
    private String razorpayKeyId;
}