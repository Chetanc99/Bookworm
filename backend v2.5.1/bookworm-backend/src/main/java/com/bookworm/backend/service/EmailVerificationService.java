package com.bookworm.backend.service;

public interface EmailVerificationService {

    /** Generates a token, emails it (see MailService), tied to this user. */
    void sendVerificationEmail(Long userId, String email);

    /** Always appears to succeed regardless of whether the email exists - same reasoning as PasswordResetService. */
    void resendVerificationEmail(String email);

    void verifyEmail(String token);
}
