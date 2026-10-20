package org.ject.support.admin.mail.dto;

import java.time.LocalDateTime;
import org.ject.support.admin.mail.domain.MailDispatchOutboxStatus;

public record MailDispatchTransitionEvent(
        Long dispatchJobId,
        Long targetId,
        MailDispatchOutboxStatus status,
        int attemptCount,
        LocalDateTime nextAttemptAt
) {
}
