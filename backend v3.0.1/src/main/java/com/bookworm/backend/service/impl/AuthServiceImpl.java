package com.bookworm.backend.service.impl;

import com.bookworm.backend.dto.request.LoginRequest;
import com.bookworm.backend.dto.request.RegisterRequest;
import com.bookworm.backend.dto.response.AuthResponse;
import com.bookworm.backend.entity.User;
import com.bookworm.backend.entity.UserRole;
import com.bookworm.backend.exception.DuplicateResourceException;
import com.bookworm.backend.repository.UserRepository;
import com.bookworm.backend.repository.UserRoleRepository;
import com.bookworm.backend.security.JwtService;
import com.bookworm.backend.security.UserPrincipal;
import com.bookworm.backend.service.AuthService;
import com.bookworm.backend.service.EmailVerificationService;
import com.bookworm.backend.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private static final String DEFAULT_ROLE = "CUSTOMER";

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailVerificationService emailVerificationService;
    private final OtpService otpService;

    // Off by default so this is a complete, testable feature without changing
    // the login contract for anyone who hasn't opted in - see AuthResponse.otpRequired.
    @Value("${bookworm.otp.enabled:false}")
    private boolean otpEnabled;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        // Lazily create the CUSTOMER role row if the lookup tier wasn't pre-seeded -
        // keeps registration working out of the box without a manual seed script.
        UserRole role = userRoleRepository.findByRoleName("CUSTOMER")
                .or(() -> userRoleRepository.findByRoleName("ROLE_CUSTOMER"))
                .orElseGet(() -> userRoleRepository.save(UserRole.builder().roleName("CUSTOMER").build()));

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .isActive(true)
                .isEmailVerified(true)
                .build();
        user = userRepository.save(user);

        emailVerificationService.sendVerificationEmail(user.getUserId(), user.getEmail());

        UserPrincipal principal = new UserPrincipal(user);
        String token = jwtService.generateToken(principal);

        return AuthResponse.builder()
                .token(token)
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(role.getRoleName())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (AuthenticationException ex) {
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (otpEnabled) {
            otpService.generateAndSend(user);
            // No token yet - the frontend must call /auth/verify-otp with the
            // code just emailed to this address before it gets a usable JWT.
            return AuthResponse.builder()
                    .email(user.getEmail())
                    .otpRequired(true)
                    .build();
        }

        UserPrincipal principal = new UserPrincipal(user);
        String token = jwtService.generateToken(principal);

        return AuthResponse.builder()
                .token(token)
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().getRoleName())
                .build();
    }
}
