package org.ject.support.domain.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import org.ject.support.domain.project.entity.ProjectIntro;

@Builder
public record ProjectIntroResponse(
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        String imageUrl,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        int sequence
) {

    @QueryProjection
    public ProjectIntroResponse {
    }

    public static ProjectIntroResponse toResponse(ProjectIntro projectIntro) {
        return ProjectIntroResponse.builder()
                .imageUrl(projectIntro.getImageUrl())
                .sequence(projectIntro.getSequence())
                .build();
    }
}
