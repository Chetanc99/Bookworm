package com.bookworm.backend.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * One log line per controller method invocation: HTTP method, path, acting
 * principal (or "anonymous" for permitAll endpoints), outcome, and duration.
 * This is infrastructure-level request logging, distinct from AuditService's
 * DB-persisted business audit trail (AuditLogController et al.) - that one
 * records *what changed* for compliance/admin review; this one is an
 * ops-facing request log for every endpoint, mutating or not, kept in the
 * application log rather than a table since nobody needs to query "every GET
 * request ever made" the way they'd query an audit trail.
 */
@Aspect
@Component
@Slf4j
public class ApiRequestLoggingAspect {

    @Around("execution(public * com.bookworm.backend.controller..*(..))")
    public Object logRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = "?";
        String uri = joinPoint.getSignature().toShortString();

        ServletRequestAttributes attrs = currentRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            method = request.getMethod();
            uri = request.getRequestURI();
        }

        String principal = currentPrincipal();
        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            log.info("[API] {} {} by {} -> OK ({}ms)", method, uri, principal, System.currentTimeMillis() - start);
            return result;
        } catch (Throwable ex) {
            log.info("[API] {} {} by {} -> {} ({}ms)",
                    method, uri, principal, ex.getClass().getSimpleName(), System.currentTimeMillis() - start);
            throw ex;
        }
    }

    private ServletRequestAttributes currentRequestAttributes() {
        try {
            return (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        } catch (IllegalStateException ex) {
            // No request bound to this thread (e.g. a controller method invoked from a
            // test without a MockMvc/servlet context) - fall back to the join point's
            // own signature rather than failing the call over a logging concern.
            return null;
        }
    }

    private String currentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "anonymous";
    }
}
