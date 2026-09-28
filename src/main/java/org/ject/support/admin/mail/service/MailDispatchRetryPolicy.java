package org.ject.support.admin.mail.service;

import java.time.Duration;
import java.time.LocalDateTime;
import org.ject.support.external.email.exception.EmailErrorCode;
import org.springframework.stereotype.Component;

@Component
public class MailDispatchRetryPolicy {

    private static final int MAX_ATTEMPTS = 3;
    private static final Duration BASE_BACKOFF = Duration.ofSeconds(30);

    public boolean shouldRetry(String failureReason, int attemptCount) {
        return attemptCount < MAX_ATTEMPTS
                && (EmailErrorCode.EMAIL_TRANSIENT_FAILURE.getCode().equals(failureReason)
                || EmailErrorCode.TOO_MANY_EMAIL_REQUESTS.getCode().equals(failureReason));
    }

    public LocalDateTime nextAttemptAt(int attemptCount, LocalDateTime now) {
        return now.plus(BASE_BACKOFF.multipliedBy(1L << Math.max(0, attemptCount - 1)));
    }
}
