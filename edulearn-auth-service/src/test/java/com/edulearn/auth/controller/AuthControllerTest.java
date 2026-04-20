package com.edulearn.auth.controller;

import com.edulearn.auth.dto.AuthResponse;
import com.edulearn.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void register_shouldReturnCreated() throws Exception {
        AuthResponse response = AuthResponse.builder()
                .email("rashi@test.com")
                .message("Registration successful. Please login to continue.")
                .build();

        when(authService.register(any())).thenReturn(response);

        String requestBody = """
                {
                  "fullName": "Rashi Raj",
                  "email": "rashi@test.com",
                  "password": "Password@123",
                  "role": "STUDENT",
                  "mobile": "9876543210"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("rashi@test.com"))
                .andExpect(jsonPath("$.message").value("Registration successful. Please login to continue."));
    }
}