package org.ject.support.admin.apply.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.ject.support.domain.apply.domain.SelectionResult;
import org.ject.support.domain.member.CareerDetails;

public record SubmittedApplyResponse(
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        Long applyId,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String name,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String phoneNumber,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED, schemaResolution = Schema.SchemaResolution.ALL_OF_REF)
        CareerDetails careerDetails,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        SelectionResult selectionResult,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        Integer waitlistNumber,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        String note
) {
    public static SubmittedApplyResponse from(SubmittedApplyProjection projection) {
        return new SubmittedApplyResponse(
                projection.applyId(),
                projection.name(),
                projection.phoneNumber(),
                projection.careerDetails(),
                projection.selectionResult(),
                projection.waitlistNumber(),
                projection.note()
        );
    }
}
