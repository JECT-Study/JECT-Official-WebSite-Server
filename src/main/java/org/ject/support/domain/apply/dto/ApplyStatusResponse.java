package org.ject.support.domain.apply.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.ApplyStatus;

public record ApplyStatusResponse(
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        ApplyStatus status
) {
    public static ApplyStatusResponse of(Apply apply) {
        return new ApplyStatusResponse(apply.getStatus());
    }
}
