package org.ject.support.domain.project.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;

@Builder
public record ProjectDetailResponse(
        String thumbnailUrl,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        List<String> projectManagers,
        List<String> productDesigners,
        List<String> frontendDevelopers,
        List<String> backendDevelopers,
        String techStack,
        String description,
        String serviceUrl
) {

    @QueryProjection
    public ProjectDetailResponse {
    }
}
