package org.ject.support.domain.ministudy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;

@Builder
public record MiniStudyResponse(
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        Long id,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        String name,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String linkUrl,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String imageUrl,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        String summary,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        String tag
) {

    @QueryProjection
    public MiniStudyResponse {
    }
}
