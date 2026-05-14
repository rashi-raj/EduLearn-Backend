package com.edulearn.payment.controller;

import com.edulearn.payment.service.AdminPaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminPaymentController.class)
@DisplayName("AdminPaymentController Unit Tests")
class AdminPaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminPaymentService adminPaymentService;

    @Test
    void getAllPayments_shouldReturnOk() throws Exception {
        when(adminPaymentService.getAllPayments()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/admin/payments/all"))
                .andExpect(status().isOk());
    }

    @Test
    void getPaymentsByStatus_shouldReturnOk() throws Exception {
        when(adminPaymentService.getPaymentsByStatus("PAID")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/admin/payments/status/PAID"))
                .andExpect(status().isOk());
    }
}
