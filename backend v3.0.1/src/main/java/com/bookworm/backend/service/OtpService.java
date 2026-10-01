package com.bookworm.backend.service;

import com.bookworm.backend.dto.response.AuthResponse;
import com.bookworm.backend.entity.User;

public interface OtpService {

    /** Generates a fresh 6-digit code, stores it, and emails it to the user (via MailService). */
    void generateAndSend(User user);

    /**
     * Validates the code for the given email against the most recent
     * unused, unexpired LoginOtp row, marks it used, and returns a full
     * AuthResponse with a real JWT - same shape AuthServiceImpl.login
     * would have returned directly if OTP were disabled.
     */
    AuthResponse verify(String email, String code);
}
