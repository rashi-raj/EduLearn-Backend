package com.edulearn.payment.controller;

import com.edulearn.payment.dto.CreatePaymentRequest;
import com.edulearn.payment.dto.CreatePaymentResponse;
import com.edulearn.payment.dto.PaymentResponse;
import com.edulearn.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    private UUID studentId;
    private UUID courseId;

    @BeforeEach
    void setUp() {
        studentId = UUID.randomUUID();
        courseId = UUID.randomUUID();
    }

    @Test
    void createOrder_shouldReturnCreatedStatus() {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setStudentId(studentId);
        request.setCourseId(courseId);
        request.setAmount(BigDecimal.valueOf(999));
        request.setPaymentMethod("RAZORPAY");

        CreatePaymentResponse mockResponse = CreatePaymentResponse.builder()
                .paymentId(UUID.randomUUID())
                .courseId(courseId)
                .studentId(studentId)
                .amount(BigDecimal.valueOf(999))
                .currency("INR")
                .razorpayOrderId("order_123")
                .build();

        when(paymentService.createPayment(any(CreatePaymentRequest.class))).thenReturn(mockResponse);

        ResponseEntity<CreatePaymentResponse> response = paymentController.createOrder(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("order_123", response.getBody().getRazorpayOrderId());
        verify(paymentService).createPayment(any(CreatePaymentRequest.class));
    }

    @Test
    void getPaymentsByStudent_shouldReturnList() {
        when(paymentService.getPaymentsByStudent(studentId)).thenReturn(List.of());

        ResponseEntity<List<PaymentResponse>> response = paymentController.getPaymentsByStudent(studentId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(paymentService).getPaymentsByStudent(studentId);
    }

    @Test
    void hasPaid_shouldReturnTrueWhenPaid() {
        when(paymentService.hasPaid(studentId, courseId)).thenReturn(true);

        ResponseEntity<Map<String, Boolean>> response = paymentController.hasPaid(studentId, courseId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().get("paid"));
    }

    @Test
    void hasPaid_shouldReturnFalseWhenNotPaid() {
        when(paymentService.hasPaid(studentId, courseId)).thenReturn(false);

        ResponseEntity<Map<String, Boolean>> response = paymentController.hasPaid(studentId, courseId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().get("paid"));
    }

    @Test
    void ping_shouldReturnOk() {
        ResponseEntity<String> response = paymentController.ping();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Payment service is working", response.getBody());
    }
}
