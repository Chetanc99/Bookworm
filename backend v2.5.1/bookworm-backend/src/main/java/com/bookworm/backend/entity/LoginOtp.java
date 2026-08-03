package com.bookworm.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Email-based login 2FA code (see AuthServiceImpl.login / OtpService).
 * Plain userId, not a User FK - same convention as PasswordResetToken /
 * EmailVerificationToken. One row per OTP sent; a fresh login attempt
 * generates a new row rather than reusing/extending an old one.
 */
@Entity
@Table(name = "LOGIN_OTPS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "login_otp_id")
    private Long loginOtpId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "otp_code", nullable = false, length = 6)
    private String otpCode;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used", nullable = false)
    @Builder.Default
    private Boolean used = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
