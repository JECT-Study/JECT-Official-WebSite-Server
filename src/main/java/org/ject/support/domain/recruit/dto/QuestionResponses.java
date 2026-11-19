package org.ject.support.domain.recruit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record QuestionResponses(
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        List<QuestionResponse> questionResponses
) {
}
