package org.ject.support.admin.mail.dto;

import org.ject.support.admin.mail.domain.MailDispatchJobStatus;

public record MailDispatchJobSearchCondition(
        Long recruitId,
        MailDispatchJobStatus status
) {
}
