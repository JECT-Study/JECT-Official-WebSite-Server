package org.ject.support.admin.mail.dto;

import java.time.LocalDateTime;
import org.ject.support.admin.mail.domain.MailDispatchOutbox;

public record MailDispatchOutboxClaim(
        Long outboxId,
        Long dispatchJobId,
        Long applyId,
        String claimToken,
        String email,
        String subject,
        String body,
        LocalDateTime leaseUntil
) {

    public static MailDispatchOutboxClaim from(MailDispatchOutbox outbox) {
        return new MailDispatchOutboxClaim(
                outbox.getId(), outbox.getDispatchJob().getId(), outbox.getApplyId(),
                outbox.getClaimToken(), outbox.getEmail(), outbox.getSubject(), outbox.getBody(),
                outbox.getLeaseUntil());
    }
}
