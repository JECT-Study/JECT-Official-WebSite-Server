package org.ject.support.external.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.ject.support.base.UnitTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class SesRateLimiterTest extends UnitTestSupport {

    @Test
    @Timeout(2)
    void 발송_허가를_대기_한도_내_얻지_못하면_발송_전_실패한다() {
        // given
        SesRateLimiter limiter = new SesRateLimiter();
        limiter.consume(limiter.getRateLimitPerSecond(), Duration.ofMillis(10));

        // when, then
        assertThatThrownBy(() -> limiter.consume(1, Duration.ofMillis(10)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("SES rate limit wait exceeded");
    }

    @Test
    @Timeout(2)
    void 대기_중_중단_신호는_보존하고_발송_전_실패한다() {
        // given
        SesRateLimiter limiter = new SesRateLimiter();
        limiter.consume(limiter.getRateLimitPerSecond(), Duration.ofMillis(10));

        // when, then
        Thread.currentThread().interrupt();
        try {
            assertThatThrownBy(() -> limiter.consume(1, Duration.ofSeconds(2)))
                    .isInstanceOf(RuntimeException.class)
                    .hasCauseInstanceOf(InterruptedException.class);
            assertThat(Thread.currentThread().isInterrupted()).isTrue();
        } finally {
            Thread.interrupted();
        }
    }
}
