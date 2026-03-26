package org.ject.support.common.data.redis;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.ject.support.common.data.redis.resilience.ResilientCacheErrorHandler;
import org.ject.support.common.data.redis.resilience.ResilientCacheResolver;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
@RequiredArgsConstructor
public class RedisCacheConfig implements CachingConfigurer {

    private final ResilientCacheErrorHandler resilientCacheErrorHandler;
    private final RedisConnectionFactory connectionFactory;

    @Bean
    public CacheManager redisCacheManager() {
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(redisCacheConfiguration())
                .build();
    }

    @Override
    public CacheResolver cacheResolver() {
        // 서킷 오픈 시 NoOpCache로 전환해 Redis 접근을 우회, 추후 로컬 캐시로 도입 논의 필요.
        return new ResilientCacheResolver(redisCacheManager());
    }

    @Override
    public CacheErrorHandler errorHandler() {
        // Redis 관련 캐시 예외는 삼키고 비즈니스 로직이 DB 폴백을 수행하도록 한다.
        return resilientCacheErrorHandler;
    }

    private RedisCacheConfiguration redisCacheConfiguration() {
        StringRedisSerializer keySerializer = new StringRedisSerializer();

        GenericJackson2JsonRedisSerializer valueSerializer =
                new GenericJackson2JsonRedisSerializer()
                        .configure(objectMapper -> {
                            objectMapper.registerModule(new JavaTimeModule());
                            objectMapper.configure(
                                    SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                                    false
                            );
                        });

        return RedisCacheConfiguration
                .defaultCacheConfig()
                .disableCachingNullValues()
                .entryTtl(Duration.ofDays(1))
                .computePrefixWith(name -> "cache::" + name + "::")
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(keySerializer)
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer)
                );
    }
}
