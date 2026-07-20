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

    private final RedisCacheCircuitBreakerProvider circuitBreakerProvider;

    @Override
    public void handleCacheGetError(@NonNull final RuntimeException exception,
                                    @NonNull final Cache cache,
                                    @NonNull final Object key) {
        handle("get", exception, cache, key);
    }

    @Override
    public void handleCachePutError(@NonNull final RuntimeException exception,
                                    @NonNull final Cache cache,
                                    @NonNull final Object key,
                                    final Object value) {
        handle("put", exception, cache, key);
    }

    @Override
    public void handleCacheEvictError(@NonNull final RuntimeException exception,
                                      @NonNull final Cache cache,
                                      @NonNull final Object key) {
        handle("evict", exception, cache, key);
    }

    @Override
    public void handleCacheClearError(@NonNull final RuntimeException exception,
                                      @NonNull final Cache cache) {
        handle("clear", exception, cache, "*");
    }

    private void handle(final String operation,
                        final RuntimeException exception,
                        final Cache cache,
                        final Object key) {
        // 레디스 관련 예외만 swallow, 그 외의 버그성 예외는 즉시 throw
        if (!RedisCacheExceptionClassifier.isRedisRelated(exception)) {
            throw exception;
        }

        // 쓰기 작업 중 레디스 장애 발생 시, 예외를 다시 던짐
        if (RedisCacheExecutionContext.isStrictWriteEnabled() &&
                ("put".equals(operation) || "evict".equals(operation) || "clear".equals(operation))) {
            log.error("Redis cache write operation failed. Operation: {}, Cache: {}, Key: {}, Message: {}",
                    operation, cache.getName(), key, exception.getMessage());
            throw exception;
        }

        // 조회 경로에서는 레디스 장애 시 DB 폴백을 위해 예외를 삼키고, 실패 상태로 마킹
        RedisCacheExecutionContext.markFailure(cache.getName());

        // 캐시 예외를 서킷 브레이커의 실패 신호로 변환하여 기록
        CircuitBreaker circuitBreaker = circuitBreakerProvider.get(cache.getName());
        circuitBreaker.onError(0, TimeUnit.NANOSECONDS, exception);

        // 의도적으로 예외를 다시 던지지 않고, 애플리케이션은 DB 폴백 경로로 진행
        log.warn("Redis cache {} failed. cache={}, key={}, fallback=DB, message={}",
                operation,
                cache.getName(),
                key,
                exception.getMessage());
    }
}

