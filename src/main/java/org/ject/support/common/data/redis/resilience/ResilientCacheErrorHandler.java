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

    // 레디스의 상태를 추적하기 위해 캐시 이름별 서킷 브레이커를 제공
    private final RedisCacheCircuitBreakerProvider circuitBreakerProvider;

    @Override
    public void handleCacheGetError(@NonNull final RuntimeException exception,
                                    @NonNull final Cache cache,
                                    @NonNull final Object key) {
        // @Cacheable의 조회(read) 경로에서 발생하는 get 실패를 처리
        handle("get", exception, cache, key);
    }

    @Override
    public void handleCachePutError(@NonNull final RuntimeException exception,
                                    @NonNull final Cache cache,
                                    @NonNull final Object key,
                                    final Object value) {
        // @Cacheable이 DB 조회 후 캐시를 갱신하려고 할 때 발생하는 put 실패를 처리
        handle("put", exception, cache, key);
    }

    @Override
    public void handleCacheEvictError(@NonNull final RuntimeException exception,
                                      @NonNull final Cache cache,
                                      @NonNull final Object key) {
        // evict 실패가 비즈니스 흐름을 끊지 않도록 서킷 브레이커에 기록하고 계속 진행
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

        // 쓰기 작업(put, evict, clear) 중 레디스 장애 발생 시,
        // 데이터 정합성을 위해 예외를 던져, 상위 비즈니스 로직(트랜잭션 등)이 롤백되거나 에러를 인지
        if ("put".equals(operation) || "evict".equals(operation) || "clear".equals(operation)) {
            log.error("Redis cache write operation failed. Operation: {}, Cache: {}, Key: {}, Message: {}",
                    operation, cache.getName(), key, exception.getMessage());
            throw exception;
        }

        // 조회(get) 경로에서는 레디스 장애 시 DB 폴백을 위해 예외를 삼키고,
        // Aspect에서 잘못된 성공을 기록하지 않도록 이 캐시를 실패 상태로 마킹
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

