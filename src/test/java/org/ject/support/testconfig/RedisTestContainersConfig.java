package org.ject.support.testconfig;

import java.time.Duration;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Profile("test")
@TestConfiguration
@Testcontainers
public class RedisTestContainersConfig implements DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(RedisTestContainersConfig.class);
    private static final int REDIS_PORT = 6379;

    @Container
    private static final GenericContainer<?> redisContainer = new GenericContainer<>("redis:7.4-alpine")
            .withExposedPorts(REDIS_PORT)
            .withStartupTimeout(Duration.ofSeconds(180));

    static {
        log.info("Redis TestContainer 초기화 시작. Docker Image: {}", redisContainer.getDockerImageName());
        try {
            if (!redisContainer.isRunning()) {
                log.info("Redis TestContainer 시작 시도...");
                redisContainer.start();
                log.info("Redis TestContainer 시작 성공. Host={}, Port={}", redisContainer.getHost(), redisContainer.getMappedPort(REDIS_PORT));
            } else {
                 log.info("Redis TestContainer 이미 실행 중. Host={}, Port={}", redisContainer.getHost(), redisContainer.getMappedPort(REDIS_PORT));
            }
        } catch (Exception e) {
            log.error("Redis TestContainer 시작 실패", e);
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
}
