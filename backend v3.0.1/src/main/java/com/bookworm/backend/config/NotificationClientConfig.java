package com.bookworm.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * RestClient pointed at bookworm-notification-service's configurable base URL.
 * The bean always exists (cheap - just holds config), but it's only ever
 * called by RemoteNotificationMailServiceImpl, which itself only activates
 * when bookworm.notification-service.enabled=true.
 */
@Configuration
public class NotificationClientConfig {

    @Bean
    public RestClient notificationServiceRestClient(
            @Value("${bookworm.notification-service.base-url:http://localhost:8082}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }
}
