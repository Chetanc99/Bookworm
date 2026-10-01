package com.bookworm.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * PasswordEncoder was originally a @Bean method on SecurityConfig itself.
 * Once CustomOidcUserService started depending on PasswordEncoder (to hash a
 * random placeholder password for Google-provisioned accounts) and
 * SecurityConfig started depending on CustomOidcUserService (constructor
 * injection, to wire oauth2Login), that created a real circular dependency:
 * SecurityConfig -> CustomOidcUserService -> PasswordEncoder -> (bean method
 * lives on SecurityConfig, which needs to finish constructing first).
 * Pulling PasswordEncoder out into its own tiny, dependency-free
 * @Configuration class breaks the cycle - nothing about the encoder itself
 * needs to live inside SecurityConfig.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
