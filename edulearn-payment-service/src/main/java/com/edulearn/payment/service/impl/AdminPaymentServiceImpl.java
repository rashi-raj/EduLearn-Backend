package com.edulearn.payment.service.impl;

import com.edulearn.payment.dto.AdminPaymentResponse;
import com.edulearn.payment.entity.Payment;
import com.edulearn.payment.repository.PaymentRepository;
import com.edulearn.payment.service.AdminPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminPaymentServiceImpl implements AdminPaymentService {

    private final PaymentRepository paymentRepository;

    @Override
    public List<AdminPaymentResponse> getAllPayments() {
        return paymentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private AdminPaymentResponse mapToResponse(Payment payment) {
        return AdminPaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .studentId(payment.getStudentId())
                .studentName(payment.getStudentName())
                .studentEmail(payment.getStudentEmail())
                .courseId(payment.getCourseId())
                .courseTitle(payment.getCourseTitle())
                .amount(payment.getAmount().doubleValue())
                .currency(payment.getCurrency())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus().name())
                .receipt(payment.getReceipt())
                .razorpayOrderId(payment.getRazorpayOrderId())
                .razorpayPaymentId(payment.getRazorpayPaymentId())
                .createdAt(payment.getCreatedAt())
                .paidAt(payment.getPaidAt())
                .build();
    }
}