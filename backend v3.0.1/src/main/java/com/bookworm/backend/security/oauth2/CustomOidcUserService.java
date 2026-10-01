package com.bookworm.backend.security.oauth2;

import com.bookworm.backend.entity.User;
import com.bookworm.backend.entity.UserRole;
import com.bookworm.backend.repository.UserRepository;
import com.bookworm.backend.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Handles the Google sign-in callback: finds or provisions a User from the
 * Google ID token, then wraps Spring Security's OidcUser with our own
 * ROLE_* authority so the rest of the app (and OAuth2AuthenticationSuccessHandler,
 * which mints the same JWT AuthServiceImpl issues) sees a consistent principal.
 *
 * Users provisioned this way get a random, never-used password hash - they can
 * only ever authenticate via Google since nothing sets/knows a real password for
 * them. If a CUSTOMER/ADMIN account with the same email already exists (created
 * via normal email/password registration), we log into that existing account
 * instead of creating a duplicate - Google's email is treated as pre-verified,
 * consistent with Google requiring the user to control the mailbox already.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOidcUserService extends OidcUserService {

    private static final String DEFAULT_ROLE = "CUSTOMER";

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        String email = oidcUser.getEmail();
        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("email_not_found"), "Google account has no email");
        }

        User user = userRepository.findByEmail(email).orElseGet(() -> provisionUser(email, oidcUser.getFullName()));

        List<GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().getRoleName()));

        return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo(), "email");
    }

    private User provisionUser(String email, String fullName) {
        UserRole role = userRoleRepository.findByRoleName(DEFAULT_ROLE)
                .orElseGet(() -> userRoleRepository.save(UserRole.builder().roleName(DEFAULT_ROLE).build()));

        User user = User.builder()
                .fullName((fullName == null || fullName.isBlank()) ? email : fullName)
                .email(email)
                .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(role)
                .isActive(true)
                .isEmailVerified(true)
                .build();

        log.info("Provisioning new user from Google sign-in: {}", email);
        return userRepository.save(user);
    }
}
