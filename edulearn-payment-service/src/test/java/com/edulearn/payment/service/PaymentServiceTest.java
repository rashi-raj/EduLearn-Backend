package com.edulearn.payment.service;

import com.edulearn.payment.config.RazorpayProperties;
import com.edulearn.payment.dto.CreatePaymentRequest;
import com.edulearn.payment.dto.CreatePaymentResponse;
import com.edulearn.payment.dto.PaymentResponse;
import com.edulearn.payment.dto.VerifyPaymentRequest;
import com.edulearn.payment.entity.Payment;
import com.edulearn.payment.entity.PaymentStatus;
import com.edulearn.payment.event.NotificationEventPublisher;
import com.edulearn.payment.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.OrderClient;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Unit Tests")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RazorpayProperties razorpayProperties;

    @Mock
    private NotificationEventPublisher notificationEventPublisher;

    @InjectMocks
    private PaymentService paymentService;

    private CreatePaymentRequest createRequest;
    private Payment payment;

    @BeforeEach
    void setUp() {
        createRequest = new CreatePaymentRequest();
        createRequest.setCourseId(UUID.randomUUID());
        createRequest.setStudentId(UUID.randomUUID());
        createRequest.setAmount(BigDecimal.valueOf(100));
        createRequest.setCourseTitle("Java Masterclass");

        payment = new Payment();
        payment.setPaymentId(UUID.randomUUID());
        payment.setRazorpayOrderId("order_123");
        payment.setStudentId(UUID.randomUUID());
        payment.setCourseTitle("Java Masterclass");
        payment.setAmount(BigDecimal.valueOf(100));
        payment.setPaymentStatus(PaymentStatus.CREATED);
    }

    @Test
    void createPayment_shouldWork() {
        try (MockedConstruction<RazorpayClient> mocked = mockConstruction(RazorpayClient.class, (mock, context) -> {
            mock.orders = mock(OrderClient.class);
            Order order = mock(Order.class);
            when(order.get("id")).thenReturn("order_123");
            when(mock.orders.create(any(JSONObject.class))).thenReturn(order);
        })) {
            when(razorpayProperties.getKeyId()).thenReturn("key");
            when(razorpayProperties.getKeySecret()).thenReturn("secret");
            when(razorpayProperties.getCurrency()).thenReturn("INR");
            when(paymentRepository.save(any())).thenReturn(payment);

            CreatePaymentResponse response = paymentService.createPayment(createRequest);

            assertNotNull(response);
            assertEquals("order_123", response.getRazorpayOrderId());
            verify(paymentRepository).save(any());
        }
    }

    @Test
    void verifyPayment_shouldUpdateStatusAndNotify() {
        VerifyPaymentRequest verifyRequest = new VerifyPaymentRequest();
        verifyRequest.setRazorpayOrderId("order_123");
        verifyRequest.setRazorpayPaymentId("pay_123");
        verifyRequest.setRazorpaySignature("sig_123");

        when(paymentRepository.findByRazorpayOrderId("order_123")).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.verifyPayment(verifyRequest);

        assertEquals("PAID", response.getPaymentStatus());
        verify(paymentRepository).saveAndFlush(payment);
        verify(notificationEventPublisher).publish(any());
    }

    @Test
    void getPaymentsByStudent_shouldReturnList() {
        UUID studentId = UUID.randomUUID();
        when(paymentRepository.findByStudentId(studentId)).thenReturn(List.of(payment));

        List<PaymentResponse> responses = paymentService.getPaymentsByStudent(studentId);

        assertEquals(1, responses.size());
    }

    @Test
    void hasPaid_shouldReturnTrue() {
        UUID studentId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        when(paymentRepository.existsByStudentIdAndCourseIdAndPaymentStatus(studentId, courseId, PaymentStatus.PAID))
                .thenReturn(true);

        assertTrue(paymentService.hasPaid(studentId, courseId));
    }
}
