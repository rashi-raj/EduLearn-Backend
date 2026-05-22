package com.edulearn.payment.service;

import com.edulearn.payment.dto.AdminPaymentResponse;

import java.util.List;

public interface AdminPaymentService {
    List<AdminPaymentResponse> getAllPayments();
    List<AdminPaymentResponse> getPaymentsByStatus(String status);
    void redirectPaymentToInstructor(java.util.UUID paymentId);
}