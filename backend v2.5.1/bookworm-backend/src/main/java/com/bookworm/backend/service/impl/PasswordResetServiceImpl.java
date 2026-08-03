package com.bookworm.backend.service.impl;

import com.bookworm.backend.entity.PasswordResetToken;
import com.bookworm.backend.entity.User;
import com.bookworm.backend.repository.PasswordResetTokenRepository;
import com.bookworm.backend.repository.UserRepository;
import com.bookworm.backend.service.MailService;
import com.bookworm.backend.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Sends via MailService, which logs instead of really emailing until
 * bookworm.mail.enabled=true and real SMTP creds are configured (see
 * MailServiceImpl) - every other piece of this flow (token generation/
 * expiry/single-use redemption) is already production-shaped regardless.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    @Value("${bookworm.password-reset.expiration-minutes:30}")
    private long expirationMinutes;

    @Value("${bookworm.frontend.reset-password-url:http://localhost:5173/reset-password}")
    private String resetPasswordUrl;

    @Override
    @Transactional
    public void requestReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            passwordResetTokenRepository.deleteByUserId(user.getUserId());

            String token = UUID.randomUUID().toString();
            passwordResetTokenRepository.save(PasswordResetToken.builder()
                    .userId(user.getUserId())
                    .token(token)
                    .expiresAt(LocalDateTime.now().plusMinutes(expirationMinutes))
                    .build());

            String resetLink = resetPasswordUrl + "?token=" + token;
            mailService.send(
                    email,
                    "Reset your Bookworm password",
                    "Click the link below to reset your password. This link expires in "
                            + expirationMinutes + " minutes.\n\n" + resetLink
                            + "\n\nIf you didn't request this, you can ignore this email.");
        });
        // Deliberately no else-branch / no exception for unknown emails - see interface doc.
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token"));

        if (Boolean.TRUE.equals(resetToken.getUsed()) || resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invalid or expired reset token");
        }

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }
}
