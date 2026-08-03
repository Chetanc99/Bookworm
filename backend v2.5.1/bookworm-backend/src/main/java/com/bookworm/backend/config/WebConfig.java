package com.bookworm.backend.config;

import com.bookworm.backend.security.UserOwnershipInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final UserOwnershipInterceptor userOwnershipInterceptor;

    @Value("${bookworm.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userOwnershipInterceptor)
                .addPathPatterns("/api/v1/users/{userId}/**");
    }

    // Serves whatever FileStorageServiceImpl writes to bookworm.upload.dir at /uploads/**.
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = "file:" + java.nio.file.Path.of(uploadDir).toAbsolutePath().normalize() + "/";
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }
}
