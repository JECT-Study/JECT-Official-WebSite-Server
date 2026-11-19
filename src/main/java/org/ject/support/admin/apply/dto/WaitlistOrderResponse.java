package org.ject.support.admin.apply.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.ject.support.domain.member.CareerDetails;

public record WaitlistOrderResponse(
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        Long applyId,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String name,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String phoneNumber,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED, schemaResolution = Schema.SchemaResolution.ALL_OF_REF)
        CareerDetails careerDetails,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        Integer waitlistNumber
) {
}
