package org.ject.support.admin.mail.dto;

import java.time.LocalDateTime;
import org.ject.support.admin.mail.domain.MailDispatchJob;
import org.ject.support.admin.mail.domain.MailDispatchJobStatus;

public record MailDispatchJobResponse(
        Long dispatchJobId,
        Long scenarioId,
        Long recruitId,
        Long requestedByAdminId,
        MailDispatchJobStatus status,
        int targetCount,
        int processingCount,
        int successCount,
        int failedCount,
        LocalDateTime requestedAt,
        LocalDateTime startedAt,
        LocalDateTime finishedAt
) {

    public static MailDispatchJobResponse from(MailDispatchJob job) {
        return new MailDispatchJobResponse(
                job.getId(),
                job.getScenarioId(),
                job.getRecruitId(),
                job.getRequestedByAdminId(),
                job.getStatus(),
                job.getTargetCount(),
                job.getProcessingCount(),
                job.getSuccessCount(),
                job.getFailedCount(),
                job.getRequestedAt(),
                job.getStartedAt(),
                job.getFinishedAt()
        );
    }
}
