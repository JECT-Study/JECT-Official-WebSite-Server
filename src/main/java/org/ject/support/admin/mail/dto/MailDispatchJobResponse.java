package org.ject.support.admin.mail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDateTime;
import org.ject.support.admin.mail.domain.MailDispatchJob;
import org.ject.support.admin.mail.domain.MailDispatchJobStatus;

public record MailDispatchJobResponse(
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        Long dispatchJobId,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        Long scenarioId,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        Long recruitId,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        Long requestedByAdminId,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        MailDispatchJobStatus status,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        int targetCount,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        int processingCount,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        int successCount,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        int failedCount,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        int unknownCount,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime requestedAt,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime startedAt,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime finishedAt,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        Instant scheduledAt,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        int excludedCount
) {

    public MailDispatchJobResponse(Long dispatchJobId, Long scenarioId, Long recruitId, Long requestedByAdminId,
                                   MailDispatchJobStatus status, int targetCount, int processingCount,
                                   int successCount, int failedCount, int unknownCount,
                                   LocalDateTime requestedAt, LocalDateTime startedAt, LocalDateTime finishedAt,
                                   Instant scheduledAt) {
        this(dispatchJobId, scenarioId, recruitId, requestedByAdminId, status, targetCount, processingCount,
                successCount, failedCount, unknownCount, requestedAt, startedAt, finishedAt, scheduledAt, 0);
    }

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
                job.getUnknownCount(),
                job.getRequestedAt(),
                job.getStartedAt(),
                job.getFinishedAt(),
                job.getScheduledAt(),
                job.getExcludedCount()
        );
    }
}
