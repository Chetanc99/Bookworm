package com.bookworm.backend.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Wraps every service.impl method (every *ServiceImpl bean's public methods,
 * since Spring AOP proxies only intercept calls that go through the bean,
 * i.e. cross-object calls - self-invocation within a class bypasses this,
 * same limitation as @Transactional) and logs how long it took. Kept
 * separate from the exception-logging aspect below rather than folding both
 * into one @Around, so each aspect stays a single, reusable concern that
 * could be applied to a narrower or wider pointcut independently later.
 *
 * Threshold-gated at INFO (>200ms) vs DEBUG (everything else) so normal
 * request-log volume isn't dominated by every single fast repository-backed
 * call - slow ones are the ones worth seeing by default.
 */
@Aspect
@Component
@Slf4j
public class ServiceExecutionTimeAspect {

    private static final long SLOW_THRESHOLD_MS = 200;

    @Around("execution(public * com.bookworm.backend.service.impl..*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long elapsedMs = System.currentTimeMillis() - start;
            String signature = joinPoint.getSignature().toShortString();
            if (elapsedMs >= SLOW_THRESHOLD_MS) {
                log.info("[TIMING] {} took {}ms", signature, elapsedMs);
            } else {
                log.debug("[TIMING] {} took {}ms", signature, elapsedMs);
            }
        }
    }
}
