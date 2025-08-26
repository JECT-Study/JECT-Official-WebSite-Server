package org.ject.support.external.infrastructure;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class SlackRateLimiter {

    private static final int RATE_LIMIT_PER_SECOND = 1;
    private final Bucket bucket;

    public SlackRateLimiter() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(RATE_LIMIT_PER_SECOND)
                .refillGreedy(RATE_LIMIT_PER_SECOND, Duration.ofSeconds(1))
                .build();

        this.bucket = Bucket.builder()
                .addLimit(limit)
                .build();
    }

    // 반환값이 boolean: 토큰이 있으면 true, 없으면 false
    public boolean tryConsume(int permits) {
        return bucket.tryConsume(permits);
    }
}