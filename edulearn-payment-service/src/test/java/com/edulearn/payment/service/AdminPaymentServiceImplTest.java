package com.edulearn.payment.service;

import com.edulearn.payment.dto.AdminPaymentResponse;
import com.edulearn.payment.entity.Payment;
import com.edulearn.payment.entity.PaymentStatus;
import com.edulearn.payment.repository.PaymentRepository;
import com.edulearn.payment.service.impl.AdminPaymentServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminPaymentServiceImpl Unit Tests")
class AdminPaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private AdminPaymentServiceImpl adminPaymentService;

    @Test
    void getAllPayments_shouldReturnList() {
        Payment payment = new Payment();
        payment.setAmount(BigDecimal.valueOf(100));
        payment.setPaymentStatus(PaymentStatus.PAID);
        
        when(paymentRepository.findAll()).thenReturn(List.of(payment));

        List<AdminPaymentResponse> responses = adminPaymentService.getAllPayments();

        assertEquals(1, responses.size());
    }

    @Test
    void getPaymentsByStatus_shouldFilter() {
        Payment payment = new Payment();
        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setAmount(java.math.BigDecimal.TEN);

        when(paymentRepository.findByPaymentStatus(PaymentStatus.PAID)).thenReturn(List.of(payment));

        List<AdminPaymentResponse> responses = adminPaymentService.getPaymentsByStatus("PAID");

        assertEquals(1, responses.size());
    }
}
