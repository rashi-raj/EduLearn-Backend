package com.edulearn.payment;

import com.edulearn.payment.dto.*;
import com.edulearn.payment.entity.Payment;
import com.edulearn.payment.entity.PaymentStatus;
import com.edulearn.payment.event.NotificationEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Lombok-generated Methods Coverage Test")
class LombokTest {

    @Test
    void testDataClasses() {
        // Payment Entity
        Payment p = new Payment();
        p.setPaymentId(UUID.randomUUID());
        p.setAmount(BigDecimal.TEN);
        p.setPaymentStatus(PaymentStatus.PAID);
        assertEquals(BigDecimal.TEN, p.getAmount());
        assertNotNull(p.toString());

        // DTOs
        CreatePaymentRequest req = new CreatePaymentRequest();
        req.setAmount(BigDecimal.ONE);
        assertEquals(BigDecimal.ONE, req.getAmount());

        CreatePaymentResponse resp = CreatePaymentResponse.builder().paymentId(UUID.randomUUID()).build();
        assertNotNull(resp.getPaymentId());

        PaymentResponse pResp = PaymentResponse.builder().paymentStatus("PAID").build();
        assertEquals("PAID", pResp.getPaymentStatus());

        AdminPaymentResponse aResp = AdminPaymentResponse.builder().amount(0.0).build();
        assertEquals(0.0, aResp.getAmount());

        VerifyPaymentRequest vReq = new VerifyPaymentRequest();
        vReq.setRazorpayOrderId("O");
        assertEquals("O", vReq.getRazorpayOrderId());

        // Event
        NotificationEvent event = NotificationEvent.builder().title("T").build();
        assertEquals("T", event.getTitle());
    }
}
