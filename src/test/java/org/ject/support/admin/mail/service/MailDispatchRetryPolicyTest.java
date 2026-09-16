package org.ject.support.admin.mail.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.ject.support.external.email.exception.EmailErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MailDispatchRetryPolicyTest {

    private final MailDispatchRetryPolicy retryPolicy = new MailDispatchRetryPolicy();

    @Test
    @DisplayName("일시적인 오류는 세 번째 시도 전까지 재시도한다")
    void 일시적인_오류는_세_번째_시도_전까지_재시도한다() {
        assertThat(retryPolicy.shouldRetry(EmailErrorCode.EMAIL_TRANSIENT_FAILURE.getCode(), 1)).isTrue();
        assertThat(retryPolicy.shouldRetry(EmailErrorCode.EMAIL_TRANSIENT_FAILURE.getCode(), 2)).isTrue();
        assertThat(retryPolicy.shouldRetry(EmailErrorCode.EMAIL_TRANSIENT_FAILURE.getCode(), 3)).isFalse();
    }

    @Test
    @DisplayName("재시도 간격은 30초와 60초로 증가한다")
    void 재시도_간격은_30초와_60초로_증가한다() {
        // given
        LocalDateTime now = LocalDateTime.of(2026, 9, 16, 12, 0);

        // when
        LocalDateTime first = retryPolicy.nextAttemptAt(1, now);
        LocalDateTime second = retryPolicy.nextAttemptAt(2, now);

        // then
        assertThat(first).isEqualTo(now.plusSeconds(30));
        assertThat(second).isEqualTo(now.plusSeconds(60));
    }

    @Test
    @DisplayName("영구 오류는 재시도하지 않는다")
    void 영구_오류는_재시도하지_않는다() {
        assertThat(retryPolicy.shouldRetry(EmailErrorCode.EMAIL_SEND_FAILURE.getCode(), 1)).isFalse();
    }
}
