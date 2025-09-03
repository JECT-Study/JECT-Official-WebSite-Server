package org.ject.support.external.infrastructure;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class SlackRateLimiter {

    private static final int MAX_TOKENS = 5;
    private static final int RATE_LIMIT_PER_SECOND = 5;
    private static final int REFILL_DURATION_SECONDS = 10;
    private final Bucket bucket;

    public SlackRateLimiter() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(MAX_TOKENS)
                .refillGreedy(RATE_LIMIT_PER_SECOND, Duration.ofSeconds(REFILL_DURATION_SECONDS))
                .build();

        this.bucket = Bucket.builder()
                .addLimit(limit)
                .build();
    }

    // 반환값 boolean: 토큰이 있으면 true, 없으면 false
    public boolean tryConsume(int permits) {
        return bucket.tryConsume(permits);
    }
}
