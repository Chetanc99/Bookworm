package com.bookworm.backend.config;

import com.bookworm.backend.entity.User;
import com.bookworm.backend.entity.UserRole;
import com.bookworm.backend.repository.UserRepository;
import com.bookworm.backend.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

/**
 * Registration always issues the CUSTOMER role - there's no other way to
 * create an ADMIN yet. This seeds exactly one ADMIN account from env vars
 * on startup, only if ADMIN_EMAIL/ADMIN_PASSWORD are both set and no user
 * with that email already exists. Safe to leave the env vars unset in prod
 * once a real admin-management flow exists; this is a bootstrap convenience,
 * not the intended long-term way to create admins.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrapConfig {

    private static final String ADMIN_ROLE = "ADMIN";

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner seedAdmin(
            @Value("${ADMIN_EMAIL:}") String adminEmail,
            @Value("${ADMIN_PASSWORD:}") String adminPassword) {
        return args -> {
            if (!StringUtils.hasText(adminEmail) || !StringUtils.hasText(adminPassword)) {
                return;
            }
            if (userRepository.existsByEmail(adminEmail)) {
                return;
            }

            UserRole adminRole = userRoleRepository.findByRoleName(ADMIN_ROLE)
                    .orElseGet(() -> userRoleRepository.save(UserRole.builder().roleName(ADMIN_ROLE).build()));

            userRepository.save(User.builder()
                    .fullName("Admin")
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode(adminPassword))
                    .role(adminRole)
                    .build());

            log.info("Seeded initial ADMIN account for {}", adminEmail);
        };
    }
}
