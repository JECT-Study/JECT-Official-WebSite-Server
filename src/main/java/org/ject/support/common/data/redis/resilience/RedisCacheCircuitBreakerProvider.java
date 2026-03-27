package org.ject.support.common.data.redis.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisCacheCircuitBreakerProvider {

    // 이름 기반의 서킷 브레이커 인스턴스를 관리하고 재사용하는 전역 레지스트리
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public CircuitBreaker get(final String cacheName) {
        // 캐시별로 서킷 브레이커를 분리하여 도메인 캐시 간 장애 전파 방지
        return circuitBreakerRegistry.circuitBreaker(
                "redis-cache-" + cacheName,
                // 레디스 전용 공통 서킷 브레이커 설정을 적용
                RedisCacheCircuitBreakerConfig.REDIS_CACHE_CONFIG_NAME
        );
    }
}

