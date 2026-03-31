package org.ject.support.common.data.redis.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisCacheCircuitBreakerConfig {

    // 각 캐시별 서킷 브레이커 인스턴스에 적용될 공통 설정 템플릿 이름
    public static final String REDIS_CACHE_CONFIG_NAME = "redis-cache";

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                // 실패율이 50% 이상일 때 서킷을 OPEN
                .failureRateThreshold(50)
                // 너무 적은 수로 서킷 OPEN 방지하기 위해 최소 호출 횟수를 5회로 제한
                .minimumNumberOfCalls(5)
                // 캐시별 서킷 브레이커에서 최근 10개의 호출 계산
                .slidingWindowSize(10)
                // OPEN 상태에서 10초 동안 대기한 후 HALF_OPEN 상태로 전환합니다.
                .waitDurationInOpenState(Duration.ofSeconds(10))
                // HALF_OPEN 상태에서 재시도할 호출 횟수를 3회로 제한
                .permittedNumberOfCallsInHalfOpenState(3)
                // 레디스 관련 장애만 서킷 브레이커 지표에 기록
                .recordException(RedisCacheExceptionClassifier::isRedisRelated)
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.ofDefaults();
        registry.addConfiguration(REDIS_CACHE_CONFIG_NAME, config);
        return registry;
    }
}

