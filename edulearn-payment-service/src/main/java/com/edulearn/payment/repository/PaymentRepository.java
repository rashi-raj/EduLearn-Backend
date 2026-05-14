package com.edulearn.payment.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.edulearn.payment.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByStudentId(UUID studentId);
    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);
    boolean existsByStudentIdAndCourseIdAndPaymentStatus(UUID studentId, UUID courseId, com.edulearn.payment.entity.PaymentStatus paymentStatus);
    List<Payment> findByPaymentStatus(com.edulearn.payment.entity.PaymentStatus paymentStatus);
}