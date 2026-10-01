package com.bookworm.backend.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Centralizes "an exception came out of a service method" logging so
 * individual services don't each need their own try/catch-and-log
 * boilerplate purely for observability - GlobalExceptionHandler still owns
 * turning the exception into an HTTP response; this just makes sure it's
 * visible in the application log with full context (which method, what
 * arguments) before that happens. Expected client errors (bad input,
 * not-found, duplicate) are logged at WARN since they're normal traffic, not
 * bugs; anything else is logged at ERROR with the full stack trace.
 */
@Aspect
@Component
@Slf4j
public class ServiceExceptionLoggingAspect {

    @AfterThrowing(pointcut = "execution(public * com.bookworm.backend.service.impl..*(..))", throwing = "ex")
    public void logException(JoinPoint joinPoint, Throwable ex) {
        String signature = joinPoint.getSignature().toShortString();
        String simpleName = ex.getClass().getSimpleName();

        if (isExpectedClientError(simpleName)) {
            log.warn("[EXCEPTION] {} threw {}: {}", signature, simpleName, ex.getMessage());
        } else {
            log.error("[EXCEPTION] {} threw {}: {}", signature, simpleName, ex.getMessage(), ex);
        }
    }

    private boolean isExpectedClientError(String exceptionSimpleName) {
        return exceptionSimpleName.equals("ResourceNotFoundException")
                || exceptionSimpleName.equals("DuplicateResourceException")
                || exceptionSimpleName.equals("IllegalArgumentException");
    }
}
