package org.ject.support.common.data.redis.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisCacheCircuitBreakerConfig {

    // Shared named config template applied to each cache-specific breaker instance.
    public static final String REDIS_CACHE_CONFIG_NAME = "redis-cache";

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        // Circuit breaker policy tuned for quick detection and short recovery probing.
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                // OPEN when failure rate >= 50% in current sliding window.
                .failureRateThreshold(50)
                // Avoid opening on tiny sample size.
                .minimumNumberOfCalls(5)
                // Count last 10 calls per cache breaker.
                .slidingWindowSize(10)
                // Stay OPEN for 10s before moving to HALF_OPEN.
                .waitDurationInOpenState(Duration.ofSeconds(10))
                // Allow limited trial calls while HALF_OPEN.
                .permittedNumberOfCallsInHalfOpenState(3)
                // Only redis-related failures contribute to breaker metrics.
                .recordException(RedisCacheExceptionClassifier::isRedisRelated)
                .build();

        // Start with default registry, then add our named redis profile.
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.ofDefaults();
        registry.addConfiguration(REDIS_CACHE_CONFIG_NAME, config);
        return registry;
    }
}

