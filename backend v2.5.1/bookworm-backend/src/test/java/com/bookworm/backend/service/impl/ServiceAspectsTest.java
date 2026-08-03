package com.bookworm.backend.service.impl;

import com.bookworm.backend.aspect.ServiceExceptionLoggingAspect;
import com.bookworm.backend.aspect.ServiceExecutionTimeAspect;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Deliberately placed in com.bookworm.backend.service.impl (not
 * com.bookworm.backend.aspect) so GreeterImpl's package actually matches
 * both aspects' `execution(public * com.bookworm.backend.service.impl..*(..))`
 * pointcut - otherwise this would prove nothing about whether the pointcut
 * itself is wired correctly. Uses AspectJProxyFactory instead of a full
 * Spring context so this stays a fast unit test.
 */
class ServiceAspectsTest {

    interface Greeter {
        String greet(String name);
        String explode();
    }

    static class GreeterImpl implements Greeter {
        @Override
        public String greet(String name) {
            return "Hello, " + name;
        }

        @Override
        public String explode() {
            throw new IllegalArgumentException("boom");
        }
    }

    @Test
    void executionTimeAspect_stillReturnsTargetMethodResult() {
        AspectJProxyFactory factory = new AspectJProxyFactory(new GreeterImpl());
        factory.addAspect(new ServiceExecutionTimeAspect());
        Greeter proxy = factory.getProxy();

        assertThat(proxy.greet("Bookworm")).isEqualTo("Hello, Bookworm");
    }

    @Test
    void exceptionLoggingAspect_stillPropagatesOriginalException() {
        AspectJProxyFactory factory = new AspectJProxyFactory(new GreeterImpl());
        factory.addAspect(new ServiceExceptionLoggingAspect());
        Greeter proxy = factory.getProxy();

        assertThatThrownBy(proxy::explode)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("boom");
    }
}
