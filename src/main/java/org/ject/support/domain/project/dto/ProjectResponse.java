package org.ject.support.domain.project.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record ProjectResponse(Long id,
                              String thumbnailUrl,
                              String name,
                              String summary,
                              String description) {

    @QueryProjection
    public ProjectResponse {
    }
}
