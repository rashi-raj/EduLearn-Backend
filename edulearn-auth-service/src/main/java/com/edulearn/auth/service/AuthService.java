package com.edulearn.auth.service;

import com.edulearn.auth.dto.AuthResponse;
import com.edulearn.auth.dto.ForgotPasswordRequest;
import com.edulearn.auth.dto.LoginRequest;
import com.edulearn.auth.dto.RegisterRequest;
import com.edulearn.auth.dto.ResetPasswordRequest;
import com.edulearn.auth.dto.UserResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserResponse getCurrentUser(String email);
    
    AuthResponse forgotPassword(ForgotPasswordRequest request);

    AuthResponse resetPassword(ResetPasswordRequest request);
}