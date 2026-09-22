package org.ject.support.admin.mail.dto;

import java.time.LocalDateTime;
import org.ject.support.admin.mail.domain.MailDispatchTarget;
import org.ject.support.admin.mail.domain.MailDispatchTargetStatus;

public record MailDispatchTargetResponse(
        Long targetId,
        Long applyId,
        String email,
        MailDispatchTargetStatus status,
        LocalDateTime sentAt,
        String failureReason
) {

    public static MailDispatchTargetResponse from(MailDispatchTarget target) {
        return new MailDispatchTargetResponse(
                target.getId(),
                target.getApplyId(),
                target.getEmail(),
                target.getStatus(),
                target.getSentAt(),
                target.getFailureReason()
        );
    }
}
