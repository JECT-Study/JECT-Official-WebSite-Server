package org.ject.support.admin.mail.dto;

import org.ject.support.admin.mail.domain.MailDispatchJob;
import org.ject.support.admin.mail.domain.MailDispatchJobStatus;

public record MailDispatchResponse(
        Long dispatchJobId,
        MailDispatchJobStatus status,
        int targetCount,
        int processingCount,
        int successCount,
        int failedCount
) {

    public static MailDispatchResponse from(MailDispatchJob job) {
        return new MailDispatchResponse(
                job.getId(),
                job.getStatus(),
                job.getTargetCount(),
                job.getProcessingCount(),
                job.getSuccessCount(),
                job.getFailedCount()
        );
    }
}
