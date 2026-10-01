package com.bookworm.backend.config;

import com.bookworm.backend.security.RateLimitFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RateLimitFilter is constructed here, not @Component-scanned, so it's
 * registered exactly once with an explicit urlPattern scoping it to
 * /api/v1/auth/* - the rest of the API is untouched.
 */
@Configuration
public class RateLimitFilterConfig {

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(
            @Value("${bookworm.rate-limit.login-max-per-minute:5}") int loginMaxPerMinute,
            @Value("${bookworm.rate-limit.default-max-per-minute:20}") int defaultMaxPerMinute) {
        FilterRegistrationBean<RateLimitFilter> registration =
                new FilterRegistrationBean<>(new RateLimitFilter(loginMaxPerMinute, defaultMaxPerMinute));
        registration.addUrlPatterns("/api/v1/auth/*");
        registration.setEnabled(true);
        return registration;
    }
}
