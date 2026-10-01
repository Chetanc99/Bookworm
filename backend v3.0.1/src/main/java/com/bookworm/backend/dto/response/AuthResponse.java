package com.bookworm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private Long userId;
    private String email;
    private String fullName;
    private String role;

    // True only when bookworm.otp.enabled is on (default off) and credentials
    // just checked out but the OTP step hasn't happened yet - in that case
    // token/userId/fullName/role are all null and the frontend must call
    // /auth/verify-otp with the code emailed to `email` before it gets a
    // real token. False (or absent, for pre-OTP clients) means this response
    // already carries a valid, usable JWT exactly like before this feature existed.
    @Builder.Default
    private boolean otpRequired = false;
}
