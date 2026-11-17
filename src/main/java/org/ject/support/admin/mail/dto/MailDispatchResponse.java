package org.ject.support.admin.mail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.ject.support.admin.mail.domain.MailDispatchJob;
import org.ject.support.admin.mail.domain.MailDispatchJobStatus;

public record MailDispatchResponse(
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        Long dispatchJobId,
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
        int excludedCount
) {

    public MailDispatchResponse(Long dispatchJobId, MailDispatchJobStatus status, int targetCount,
                                int processingCount, int successCount, int failedCount, int unknownCount) {
        this(dispatchJobId, status, targetCount, processingCount, successCount, failedCount, unknownCount, 0);
    }

    public static MailDispatchResponse from(MailDispatchJob job) {
        return new MailDispatchResponse(
                job.getId(),
                job.getStatus(),
                job.getTargetCount(),
                job.getProcessingCount(),
                job.getSuccessCount(),
                job.getFailedCount(),
                job.getUnknownCount(),
                job.getExcludedCount()
        );
    }
}
