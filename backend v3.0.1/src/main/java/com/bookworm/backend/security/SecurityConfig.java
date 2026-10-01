package com.bookworm.backend.security;

import com.bookworm.backend.security.oauth2.CustomOidcUserService;
import com.bookworm.backend.security.oauth2.OAuth2AuthenticationFailureHandler;
import com.bookworm.backend.security.oauth2.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Stateless JWT auth. /auth/** and Swagger are public; everything else
 * requires a valid Bearer token. NOTE for the next dev: controllers still
 * take userId as a @PathVariable rather than reading it off the
 * authenticated principal - that's the natural follow-up once this lands
 * (e.g. add a check that the token's userId matches the path userId, or
 * switch controllers to pull it from @AuthenticationPrincipal entirely).
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AppUserDetailsService userDetailsService;
    private final JwtAuthFilter jwtAuthFilter;
    private final CustomOidcUserService customOidcUserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    // Comma-separated list of allowed frontend origins. Defaults cover both
    // Vite's default dev port and the one this project's frontend actually
    // runs on. Override via CORS_ALLOWED_ORIGINS in any other environment.
    @Value("${CORS_ALLOWED_ORIGINS:http://localhost:5173,http://127.0.0.1:5173}")
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, DaoAuthenticationProvider authenticationProvider) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // CORS preflight - browsers send this with no Authorization header,
                        // so it must be permitted before any of the ADMIN-only matchers
                        // below (which otherwise apply to all HTTP methods, OPTIONS included)
                        // reject it and block the real request from ever being sent.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // Google OAuth2/OIDC handshake endpoints - Spring Security's own
                        // filters handle these before JwtAuthFilter ever gets a request.
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        // Liveness/readiness probes need to be reachable with no auth
                        // (e.g. container orchestrators); everything else under
                        // /actuator/** (metrics, full health details, etc.) is ADMIN-only.
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        // Served product images/content files - public, same as the catalog itself.
                        .requestMatchers("/uploads/**").permitAll()

                        // Public GET endpoints for catalog browsing (Guest Users & Customers)
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/products/**",
                                "/api/v1/categories/**",
                                "/api/v1/subcategories/**",
                                "/api/v1/genres/**",
                                "/api/v1/languages/**",
                                "/api/v1/library-packages/**").permitAll()

                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/categories/**",
                                "/api/v1/subcategories/**",
                                "/api/v1/genres/**",
                                "/api/v1/languages/**",
                                "/api/v1/products/**",
                                "/api/v1/stakeholders/**",
                                "/api/v1/credit-types/**",
                                "/api/v1/beneficiaries/**",
                                "/api/v1/beneficiary-types/**",
                                "/api/v1/library-packages/**")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.PUT,
                                "/api/v1/categories/**",
                                "/api/v1/subcategories/**",
                                "/api/v1/genres/**",
                                "/api/v1/languages/**",
                                "/api/v1/products/**",
                                "/api/v1/stakeholders/**",
                                "/api/v1/credit-types/**",
                                "/api/v1/beneficiaries/**",
                                "/api/v1/beneficiary-types/**",
                                "/api/v1/library-packages/**")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.DELETE,
                                "/api/v1/categories/**",
                                "/api/v1/subcategories/**",
                                "/api/v1/genres/**",
                                "/api/v1/languages/**",
                                "/api/v1/products/**",
                                "/api/v1/stakeholders/**",
                                "/api/v1/credit-types/**",
                                "/api/v1/beneficiaries/**",
                                "/api/v1/beneficiary-types/**",
                                "/api/v1/library-packages/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/api/v1/beneficiaries/*/royalties/**")
                        .hasRole("ADMIN")

                        // Cross-beneficiary distribution lookups - same sensitivity as the
                        // per-beneficiary royalty ledger above.
                        .requestMatchers("/api/v1/royalty-ledger/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/api/v1/admin/users/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/api/v1/admin/transactions/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/api/v1/admin/uploads/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/api/v1/admin/products/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/api/v1/admin/audit-logs/**")
                        .hasRole("ADMIN")

                        .anyRequest().authenticated())

                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // Google social login. The JWT session model elsewhere is unaffected -
                // this only runs during the one-time Google handshake; on success we
                // mint the same stateless JWT AuthServiceImpl issues and redirect to
                // the SPA (see OAuth2AuthenticationSuccessHandler), so every other
                // endpoint keeps using JwtAuthFilter/Authorization headers as before.
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.oidcUserService(customOidcUserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler(oAuth2AuthenticationFailureHandler));

        return http.build();
    }
}