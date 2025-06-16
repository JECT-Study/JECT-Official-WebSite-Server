package org.ject.support.external.infrastructure;

import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class SesRateLimiter {

    private static final int RATE_LIMIT_PER_SECOND = 14;

    private final Bucket bucket = Bucket.builder()
            .addLimit(limit -> limit.capacity(RATE_LIMIT_PER_SECOND)
                    .refillIntervally(RATE_LIMIT_PER_SECOND, Duration.ofSeconds(1)))
            .build();

    public void consume(int permits) {
        try {
            bucket.asBlocking().consume(permits);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Rate limiter interrupted", e);
        }
    }

    public int getRateLimitPerSecond() {
        return RATE_LIMIT_PER_SECOND;
    }
}