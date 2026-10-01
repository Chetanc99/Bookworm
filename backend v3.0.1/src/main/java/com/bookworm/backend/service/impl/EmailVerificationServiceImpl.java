package com.bookworm.backend.service.impl;

import com.bookworm.backend.entity.EmailVerificationToken;
import com.bookworm.backend.entity.User;
import com.bookworm.backend.repository.EmailVerificationTokenRepository;
import com.bookworm.backend.repository.UserRepository;
import com.bookworm.backend.service.EmailVerificationService;
import com.bookworm.backend.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final MailService mailService;

    @Value("${bookworm.email-verification.expiration-minutes:1440}")
    private long expirationMinutes;

    @Value("${bookworm.frontend.verify-email-url:http://localhost:5173/verify-email}")
    private String verifyEmailUrl;

    @Override
    @Transactional
    public void sendVerificationEmail(Long userId, String email) {
        tokenRepository.deleteByUserId(userId);

        String token = UUID.randomUUID().toString();
        tokenRepository.save(EmailVerificationToken.builder()
                .userId(userId)
                .token(token)
                .expiresAt(LocalDateTime.now().plusMinutes(expirationMinutes))
                .build());

        String verifyLink = verifyEmailUrl + "?token=" + token;
        try {
            // Swallow mail failures here (not rethrown) - registration must not fail
            // just because the outbound email couldn't be sent (see AuthServiceImpl.register,
            // which calls this from within its own @Transactional and would otherwise
            // roll back the newly-created user).
            mailService.send(
                    email,
                    "Verify your Bookworm email",
                    "Welcome to Bookworm! Click the link below to verify your email address:\n\n"
                            + verifyLink + "\n\nThis link expires in " + (expirationMinutes / 60) + " hours.");
        } catch (Exception ex) {
            log.warn("Failed to send verification email to {}: {}", email, ex.getMessage());
        }
    }

    @Override
    @Transactional
    public void resendVerificationEmail(String email) {
        userRepository.findByEmail(email)
                .filter(user -> !Boolean.TRUE.equals(user.getIsEmailVerified()))
                .ifPresent(user -> sendVerificationEmail(user.getUserId(), user.getEmail()));
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired verification token"));

        if (Boolean.TRUE.equals(verificationToken.getUsed())
                || verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invalid or expired verification token");
        }

        User user = userRepository.findById(verificationToken.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired verification token"));

        user.setIsEmailVerified(true);
        userRepository.save(user);

        verificationToken.setUsed(true);
        tokenRepository.save(verificationToken);
    }
}
