package com.bookworm.backend.service.impl;

import com.bookworm.backend.dto.response.AuthResponse;
import com.bookworm.backend.entity.LoginOtp;
import com.bookworm.backend.entity.User;
import com.bookworm.backend.repository.LoginOtpRepository;
import com.bookworm.backend.repository.UserRepository;
import com.bookworm.backend.security.JwtService;
import com.bookworm.backend.security.UserPrincipal;
import com.bookworm.backend.service.MailService;
import com.bookworm.backend.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OtpServiceImpl implements OtpService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final LoginOtpRepository loginOtpRepository;
    private final UserRepository userRepository;
    private final MailService mailService;
    private final JwtService jwtService;

    @Value("${bookworm.otp.expiration-minutes:5}")
    private int expirationMinutes;

    @Override
    @Transactional
    public void generateAndSend(User user) {
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));

        loginOtpRepository.save(LoginOtp.builder()
                .userId(user.getUserId())
                .otpCode(code)
                .expiresAt(LocalDateTime.now().plusMinutes(expirationMinutes))
                .used(false)
                .build());

        // Best-effort, same reasoning as every other MailService caller in this
        // codebase - a mail failure must not block the login attempt outright,
        // it just means the user won't receive the code (they can request a
        // fresh login to get a new one, or check server logs in dev where
        // MailServiceImpl logs instead of sending).
        try {
            mailService.send(user.getEmail(), "Your Bookworm login code",
                    "Your one-time login code is: " + code + "\nIt expires in " + expirationMinutes + " minutes.");
        } catch (Exception ignored) {
            // MailServiceImpl already logs failures internally.
        }
    }

    @Override
    @Transactional
    public AuthResponse verify(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or code"));

        LoginOtp otp = loginOtpRepository.findTopByUserIdAndUsedFalseOrderByCreatedAtDesc(user.getUserId())
                .orElseThrow(() -> new BadCredentialsException("No pending login code for this account"));

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadCredentialsException("This code has expired - please log in again to get a new one");
        }
        if (!otp.getOtpCode().equals(code)) {
            throw new BadCredentialsException("Invalid email or code");
        }

        otp.setUsed(true);
        loginOtpRepository.save(otp);

        UserPrincipal principal = new UserPrincipal(user);
        String token = jwtService.generateToken(principal);

        return AuthResponse.builder()
                .token(token)
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().getRoleName())
                .otpRequired(false)
                .build();
    }
}
