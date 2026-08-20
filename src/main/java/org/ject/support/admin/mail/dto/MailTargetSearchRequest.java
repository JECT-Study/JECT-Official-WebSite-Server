package org.ject.support.admin.mail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MailTargetSearchRequest(
        @Schema(description = "모집 공고 ID", example = "1")
        @NotNull @Positive Long recruitId,
        @Schema(description = "선정 결과 필터", example = "PASSED", nullable = true)
        MailTargetSelectionResult selectionResult
) {
}
