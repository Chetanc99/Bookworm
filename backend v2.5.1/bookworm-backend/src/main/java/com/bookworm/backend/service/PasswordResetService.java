package com.bookworm.backend.service;

public interface PasswordResetService {

    /**
     * Always appears to succeed regardless of whether the email exists
     * (standard practice - don't leak account existence). If it does exist,
     * a single-use token is generated and "sent" - see impl note on the
     * missing mail integration (tracked separately under email receipts/
     * verification).
     */
    void requestReset(String email);

    void resetPassword(String token, String newPassword);
}
