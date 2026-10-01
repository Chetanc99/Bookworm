package com.bookworm.backend.controller;

import com.bookworm.backend.dto.request.ForgotPasswordRequest;
import com.bookworm.backend.dto.request.LoginRequest;
import com.bookworm.backend.dto.request.RegisterRequest;
import com.bookworm.backend.dto.request.ResetPasswordRequest;
import com.bookworm.backend.dto.request.VerifyOtpRequest;
import com.bookworm.backend.dto.response.ApiResponse;
import com.bookworm.backend.dto.response.AuthResponse;
import com.bookworm.backend.service.AuthService;
import com.bookworm.backend.service.EmailVerificationService;
import com.bookworm.backend.service.OtpService;
import com.bookworm.backend.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Registration and login - issues a JWT to use as a Bearer token on every other endpoint")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final EmailVerificationService emailVerificationService;
    private final OtpService otpService;

    @PostMapping("/register")
    @Operation(summary = "Register a new customer account and receive a JWT")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account created", authService.register(request)));
    }

    @PostMapping("/login")
    @Operation(summary = "Log in and receive a JWT (or, if bookworm.otp.enabled is on, a signal that an email OTP was just sent - see /verify-otp)")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        String message = response.isOtpRequired() ? "OTP sent to your email" : "Login successful";
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Complete a login by submitting the OTP code emailed after /login (only relevant when bookworm.otp.enabled is on)")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Login successful", otpService.verify(request.getEmail(), request.getCode())));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request a password reset token (always reports success, regardless of whether the email exists)")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestReset(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(
                "If an account with that email exists, a reset link has been sent", null));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset a password using a valid, unexpired reset token")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Password reset successful", null));
    }

    @GetMapping("/verify-email")
    @Operation(summary = "Verify an email address using the token from the verification email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {
        emailVerificationService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success("Email verified", null));
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Resend the verification email (always reports success, regardless of whether the email exists)")
    public ResponseEntity<ApiResponse<Void>> resendVerification(@Valid @RequestBody ForgotPasswordRequest request) {
        emailVerificationService.resendVerificationEmail(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(
                "If an unverified account with that email exists, a new verification email has been sent", null));
    }
}
