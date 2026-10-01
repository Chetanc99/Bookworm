package com.bookworm.backend.service;

import com.bookworm.backend.dto.request.LoginRequest;
import com.bookworm.backend.dto.request.RegisterRequest;
import com.bookworm.backend.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
