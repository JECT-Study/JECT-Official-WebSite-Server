package org.ject.support.domain.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;

@Builder
public record ProjectResponse(
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        Long id,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String thumbnailUrl,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String name,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        String summary,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String description,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        String serviceType
) {

    @QueryProjection
    public ProjectResponse {
    }
}
