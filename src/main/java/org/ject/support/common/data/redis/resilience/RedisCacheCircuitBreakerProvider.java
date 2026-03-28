package org.ject.support.common.data.redis.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisCacheCircuitBreakerProvider {

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public CircuitBreaker get(final String cacheName) {
        // 캐시별로 서킷 브레이커를 분리하여 도메인 캐시 간 장애 전파 방지
        return circuitBreakerRegistry.circuitBreaker(
                "redis-cache-" + cacheName,
                // 레디스 전용 공통 서킷 브레이커 설정을 적용
                RedisCacheCircuitBreakerConfig.REDIS_CACHE_CONFIG_NAME
        );
    }

    public void resetAll() {
        // 모든 서킷 브레이커의 상태와 메트릭을 초기화하여 테스트 간 격리 등을 지원
        circuitBreakerRegistry.getAllCircuitBreakers()
                .forEach(CircuitBreaker::reset);
    }
}

