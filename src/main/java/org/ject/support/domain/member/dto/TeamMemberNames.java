package org.ject.support.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.querydsl.core.annotations.QueryProjection;
import java.util.List;

public record TeamMemberNames(
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> productManagers,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> productDesigners,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> frontendDevelopers,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> backendDevelopers
) {

    @QueryProjection
    public TeamMemberNames {
    }
}
