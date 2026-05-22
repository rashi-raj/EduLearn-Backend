package com.edulearn.payment.controller;

import com.edulearn.payment.dto.AdminPaymentResponse;
import com.edulearn.payment.service.AdminPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Payment Controller", description = "Admin APIs for payment management and reporting")
public class AdminPaymentController {

    private final AdminPaymentService adminPaymentService;

    @Operation(summary = "Get all payments (admin)")
    @GetMapping("/all")
    public ResponseEntity<List<AdminPaymentResponse>> getAllPayments() {
        log.info("Admin fetching all payments");
        return ResponseEntity.ok(adminPaymentService.getAllPayments());
    }

    @Operation(summary = "Get payments by status (admin)")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<AdminPaymentResponse>> getPaymentsByStatus(@PathVariable String status) {
        log.info("Admin fetching payments with status={}", status);
        return ResponseEntity.ok(adminPaymentService.getPaymentsByStatus(status));
    }

    @Operation(summary = "Redirect money to instructor (admin)")
    @PostMapping("/{paymentId}/redirect")
    public ResponseEntity<Void> redirectPaymentToInstructor(@PathVariable java.util.UUID paymentId) {
        log.info("Admin redirecting payment ID: {} to instructor", paymentId);
        adminPaymentService.redirectPaymentToInstructor(paymentId);
        return ResponseEntity.ok().build();
    }
}