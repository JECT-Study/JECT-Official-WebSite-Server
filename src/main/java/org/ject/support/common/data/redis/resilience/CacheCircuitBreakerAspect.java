package org.ject.support.common.data.redis.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
// Execute before Spring Cache interceptor so we can decide whether to bypass cache access.
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class CacheCircuitBreakerAspect {

    // Provider that returns one circuit breaker instance per cache name.
    private final RedisCacheCircuitBreakerProvider circuitBreakerProvider;

    // Intercepts every method that uses @Cacheable, @CachePut, or @CacheEvict.
    @Around("@annotation(org.springframework.cache.annotation.Cacheable) || " +
            "@annotation(org.springframework.cache.annotation.CachePut) || " +
            "@annotation(org.springframework.cache.annotation.CacheEvict)")
    public Object aroundCacheCall(final ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // A single method can reference multiple cache names via different annotations.
        List<String> cacheNames = extractCacheNames(method);
        // Track only circuit breakers that granted permission for this request.
        List<CircuitBreakerExecution> grantedBreakers = new ArrayList<>(cacheNames.size());
        // If at least one cache is OPEN, we force cache bypass for this invocation.
        boolean bypassEnabled = false;

        // Try to acquire permission from each cache-specific circuit breaker.
        for (String cacheName : cacheNames) {
            CircuitBreaker circuitBreaker = circuitBreakerProvider.get(cacheName);
            // OPEN state denies permission immediately (fail-fast, no redis round-trip).
            if (!circuitBreaker.tryAcquirePermission()) {
                bypassEnabled = true;
                log.debug("Cache circuit is open. cache={}, method={}", cacheName, joinPoint.getSignature());
                continue;
            }

            // Keep breaker so we can report success after method execution.
            grantedBreakers.add(new CircuitBreakerExecution(cacheName, circuitBreaker));
        }

        // Enable ThreadLocal bypass so CacheResolver returns NoOpCache.
        if (bypassEnabled) {
            RedisCacheExecutionContext.enableBypass();
        }

        try {
            // Continue to method execution; when bypass is enabled, cache layer is skipped.
            Object result = joinPoint.proceed();
            // Mark successful calls only when we actually used real cache (not bypassed).
            if (!bypassEnabled) {
                recordSuccess(grantedBreakers);
            }
            return result;
        } finally {
            // Always clear ThreadLocal to avoid leaking state across pooled threads.
            RedisCacheExecutionContext.clear();
        }
    }

    // Records success only when no redis failure was marked for that cache in this call.
    private void recordSuccess(final List<CircuitBreakerExecution> grantedBreakers) {
        for (CircuitBreakerExecution execution : grantedBreakers) {
            // If cache get/put failed, error handler already counted this call as failure.
            if (RedisCacheExecutionContext.hasFailure(execution.cacheName())) {
                continue;
            }
            // Zero duration because we only need outcome, not latency metric.
            execution.circuitBreaker().onSuccess(0, TimeUnit.NANOSECONDS);
        }
    }

    // Extract cache names from any cache-related annotation on the method.
    private List<String> extractCacheNames(final Method method) {
        Cacheable cacheable = method.getAnnotation(Cacheable.class);
        if (cacheable != null) {
            return getNames(cacheable.cacheNames(), cacheable.value());
        }

        CachePut cachePut = method.getAnnotation(CachePut.class);
        if (cachePut != null) {
            return getNames(cachePut.cacheNames(), cachePut.value());
        }

        CacheEvict cacheEvict = method.getAnnotation(CacheEvict.class);
        if (cacheEvict != null) {
            return getNames(cacheEvict.cacheNames(), cacheEvict.value());
        }

        return Collections.emptyList();
    }

    private List<String> getNames(final String[] cacheNames, final String[] value) {
        if (cacheNames.length > 0) {
            return Arrays.asList(cacheNames);
        }
        return Arrays.asList(value);
    }

    // Small immutable holder for per-cache breaker execution state.
    private record CircuitBreakerExecution(String cacheName, CircuitBreaker circuitBreaker) {
    }
}
