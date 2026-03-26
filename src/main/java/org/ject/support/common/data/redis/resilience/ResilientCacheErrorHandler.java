package org.ject.support.common.data.redis.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResilientCacheErrorHandler implements CacheErrorHandler {

    // Provides cache-name scoped circuit breakers used to track redis health.
    private final RedisCacheCircuitBreakerProvider circuitBreakerProvider;

    @Override
    public void handleCacheGetError(@NonNull final RuntimeException exception,
                                    @NonNull final Cache cache,
                                    @NonNull final Object key) {
        // get failures are most common with @Cacheable read path.
        handle("get", exception, cache, key);
    }

    @Override
    public void handleCachePutError(@NonNull final RuntimeException exception,
                                    @NonNull final Cache cache,
                                    @NonNull final Object key,
                                    final Object value) {
        // put failures happen when @Cacheable tries to populate cache after DB fetch.
        handle("put", exception, cache, key);
    }

    @Override
    public void handleCacheEvictError(@NonNull final RuntimeException exception,
                                      @NonNull final Cache cache,
                                      @NonNull final Object key) {
        // evict failures should not break business flow; mark breaker and continue.
        handle("evict", exception, cache, key);
    }

    @Override
    public void handleCacheClearError(@NonNull final RuntimeException exception,
                                      @NonNull final Cache cache) {
        // clear has no single key; use wildcard for observability log.
        handle("clear", exception, cache, "*");
    }

    // Centralized handling for all cache operations.
    private void handle(final String operation,
                        final RuntimeException exception,
                        final Cache cache,
                        final Object key) {
        // Only swallow redis-related exceptions. Non-redis bugs must still fail fast.
        if (!RedisCacheExceptionClassifier.isRedisRelated(exception)) {
            throw exception;
        }

        // Mark this cache as failed so aspect does not record false success.
        RedisCacheExecutionContext.markFailure(cache.getName());

        // Convert cache exception into circuit-breaker failure signal.
        CircuitBreaker circuitBreaker = circuitBreakerProvider.get(cache.getName());
        circuitBreaker.onError(0, TimeUnit.NANOSECONDS, exception);

        // Intentionally do not rethrow: application proceeds to DB fallback path.
        log.warn("Redis cache {} failed. cache={}, key={}, fallback=DB, message={}",
                operation,
                cache.getName(),
                key,
                exception.getMessage());
    }
}

