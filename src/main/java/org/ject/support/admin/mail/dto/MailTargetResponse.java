package org.ject.support.admin.mail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.ject.support.domain.apply.domain.SelectionResult;

public record MailTargetResponse(
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        Long applyId,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String name,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String phoneNumber,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        String email,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        SelectionResult selectionResult,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        Integer waitlistNumber
) {
    public MailTargetResponse {
        if (selectionResult != SelectionResult.WAITLISTED) {
            waitlistNumber = null;
        }
    }
}
