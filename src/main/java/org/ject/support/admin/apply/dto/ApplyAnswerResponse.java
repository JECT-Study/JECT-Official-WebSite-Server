package org.ject.support.admin.apply.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ApplyAnswerResponse(
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        Long questionId,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        String questionTitle,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String answer
) {
}
