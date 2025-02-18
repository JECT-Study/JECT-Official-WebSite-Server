package org.ject.support.domain.project.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

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
