package org.ject.support.external.email.exception;

import lombok.Getter;
import org.ject.support.common.exception.BusinessException;

@Getter
public class RateLimitException extends BusinessException {
    private final long retryAfter;

    public RateLimitException(long retryAfter) {
        super(EmailErrorCode.TOO_MANY_EMAIL_REQUESTS);
        this.retryAfter = retryAfter;
    }
}
