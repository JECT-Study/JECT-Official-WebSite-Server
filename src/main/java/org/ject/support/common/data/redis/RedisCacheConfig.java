package org.ject.support.common.data.redis;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Bean
    public CacheManager redisCacheManager(
            RedisConnectionFactory connectionFactory
    ) {
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(redisCacheConfiguration())
                .build();
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
