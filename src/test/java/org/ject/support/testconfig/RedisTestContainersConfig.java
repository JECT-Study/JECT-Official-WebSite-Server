package org.ject.support.testconfig;

import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Profile("test")
@TestConfiguration
@Testcontainers
@Slf4j
public class RedisTestContainersConfig implements DisposableBean {
    private static final int REDIS_PORT = 6379;

    // @Container 어노테이션 제거
    private static final GenericContainer<?> redisContainer = new GenericContainer<>("redis:7.4-alpine")
            .withExposedPorts(REDIS_PORT)
            .withStartupTimeout(Duration.ofSeconds(180));
    static {
        try {
            if (!redisContainer.isRunning()) {
                redisContainer.start();
                // 컨테이너가 완전히 시작될 때까지 잠시 대기
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            throw new RuntimeException("Redis 컨테이너 시작 실패", e);
        }
    }

    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        try {
            // 컨테이너가 실행 중인지 확인
            if (!redisContainer.isRunning()) {
                redisContainer.start();
            }
            
            return new LettuceConnectionFactory(
                    redisContainer.getHost(),
                    redisContainer.getMappedPort(REDIS_PORT)
            );
        } catch (Exception e) {
            throw new RuntimeException("Redis 연결 팩토리 생성 실패", e);
        }
    }

    @Override
    public void destroy() {
        if(redisContainer.isRunning()) {
            redisContainer.stop();
        }
    }

    public static void stop() {
        if (redisContainer.isRunning()) {
            redisContainer.stop();
        }
    }

    public static void start() {
        if (!redisContainer.isRunning()) {
            redisContainer.start();
        }
    }

    public static String getHost() {
        return redisContainer.getHost();
    }

    public static Integer getMappedPort() {
        return redisContainer.getMappedPort(REDIS_PORT);
    }
}
