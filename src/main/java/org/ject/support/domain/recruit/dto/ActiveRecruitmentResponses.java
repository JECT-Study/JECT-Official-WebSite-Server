package org.ject.support.domain.recruit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record ActiveRecruitmentResponses(
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        List<ActiveRecruitmentResponse> recruitments
) {
}
