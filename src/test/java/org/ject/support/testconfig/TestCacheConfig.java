package org.ject.support.testconfig;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;

/**
 * 테스트 환경에서 Redis 의존성 없이 캐시 동작을 검증하기 위한 설정.
 * RedisCacheConfig의 CacheManager 빈을 ConcurrentMapCacheManager로 대체하여,
 * 모킹된 RedisConnectionFactory로 인한 NullPointerException을 방지한다.
 */
@TestConfiguration
public class TestCacheConfig {

    @Bean
    public CacheManager redisCacheManager() {
        return new ConcurrentMapCacheManager();
    }
}
