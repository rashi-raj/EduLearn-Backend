package com.edulearn.payment.controller;

import com.edulearn.payment.dto.CreatePaymentResponse;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import com.edulearn.payment.dto.PaymentResponse;
import com.edulearn.payment.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@DisplayName("PaymentController Unit Tests")
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createPayment_shouldReturnCreated() throws Exception {
        when(paymentService.createPayment(any())).thenReturn(CreatePaymentResponse.builder().build());

        String body = """
                {
                  "courseId": "00000000-0000-0000-0000-000000000001",
                  "studentId": "00000000-0000-0000-0000-000000000002",
                  "amount": 100,
                  "paymentMethod": "UPI"
                }
                """;

        mockMvc.perform(post("/api/v1/payments/create-order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void verifyPayment_shouldReturnOk() throws Exception {
        when(paymentService.verifyPayment(any())).thenReturn(PaymentResponse.builder().build());

        String body = """
                {
                  "razorpayOrderId": "order_1",
                  "razorpayPaymentId": "pay_1",
                  "razorpaySignature": "sig_1"
                }
                """;

        mockMvc.perform(post("/api/v1/payments/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk());
    }

    @Test
    void getPaymentsByStudent_shouldReturnOk() throws Exception {
        UUID id = UUID.randomUUID();
        when(paymentService.getPaymentsByStudent(id)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/payments/student/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    void checkPayment_shouldReturnOk() throws Exception {
        UUID sid = UUID.randomUUID();
        UUID cid = UUID.randomUUID();
        when(paymentService.hasPaid(sid, cid)).thenReturn(true);

        mockMvc.perform(get("/api/v1/payments/status")
                .param("studentId", sid.toString())
                .param("courseId", cid.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void ping_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/payments/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Payment service is working"));
    }
}
