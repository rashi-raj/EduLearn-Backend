package com.edulearn.payment.controller;

import com.edulearn.payment.dto.CreatePaymentRequest;
import com.edulearn.payment.dto.CreatePaymentResponse;
import com.edulearn.payment.dto.PaymentResponse;
import com.edulearn.payment.dto.VerifyPaymentRequest;
import com.edulearn.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Controller", description = "APIs for payment order creation, verification, and student payment history")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Create a Razorpay payment order")
    @PostMapping("/create-order")
    public ResponseEntity<CreatePaymentResponse> createOrder(
            @Valid @RequestBody CreatePaymentRequest request
    ) {
        log.info("Creating payment order for studentId={}, courseId={}", request.getStudentId(), request.getCourseId());
        CreatePaymentResponse response = paymentService.createPayment(request);
        log.info("Payment order created: orderId={}", response.getRazorpayOrderId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Verify a Razorpay payment")
    @PostMapping("/verify")
    public ResponseEntity<PaymentResponse> verifyPayment(
            @Valid @RequestBody VerifyPaymentRequest request
    ) {
        log.info("Verifying payment for orderId={}", request.getRazorpayOrderId());
        return ResponseEntity.ok(paymentService.verifyPayment(request));
    }

    @Operation(summary = "Get all payments by student")
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByStudent(
            @PathVariable UUID studentId
    ) {
        log.debug("Fetching payments for studentId={}", studentId);
        return ResponseEntity.ok(paymentService.getPaymentsByStudent(studentId));
    }

    @Operation(summary = "Check if student has paid for a course")
    @GetMapping("/status")
    public ResponseEntity<Map<String, Boolean>> hasPaid(
            @RequestParam UUID studentId,
            @RequestParam UUID courseId
    ) {
        log.debug("Checking payment status for studentId={}, courseId={}", studentId, courseId);
        return ResponseEntity.ok(Map.of(
                "paid", paymentService.hasPaid(studentId, courseId)
        ));
    }

    @Operation(summary = "Health check for payment service")
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        log.debug("Ping endpoint invoked");
        return ResponseEntity.ok("Payment service is working");
    }
}