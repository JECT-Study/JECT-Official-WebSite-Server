package org.ject.support.domain.recruit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.ject.support.domain.recruit.domain.RecruitType;

public record RecruitTypeCountResponse(
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        RecruitType recruitType,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        long count
) {
}
