package org.ject.support.domain.jectalk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import org.ject.support.domain.jectalk.enums.ContentType;

@Builder
public record JectalkResponse(
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        Long id,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        String title,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        String description,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String contentUrl,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        ContentType contentType,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String thumbnailUrl,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        String summary
) {

    @QueryProjection
    public JectalkResponse {
    }
}
