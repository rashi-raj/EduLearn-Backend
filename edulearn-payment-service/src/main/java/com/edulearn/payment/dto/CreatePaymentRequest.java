package com.edulearn.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class CreatePaymentRequest {

    @NotNull
    private UUID courseId;

    @NotNull
    private UUID studentId;

    private String studentName;
    private String studentEmail;
    private String courseTitle;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotBlank
    private String paymentMethod;
}