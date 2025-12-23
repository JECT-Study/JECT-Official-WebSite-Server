package org.ject.support.domain.project.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;

@Builder
public record ProjectResponse(Long id,
                              String thumbnailUrl,
                              String name,
                              String summary,
                              String description,
                              String serviceType) {

    @QueryProjection
    public ProjectResponse {
    }
}
