package org.ject.support.domain.ministudy.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;

@Builder
public record MiniStudyResponse(Long id, String name, String linkUrl, String imageUrl, String summary) {

    @QueryProjection
    public MiniStudyResponse {
    }
}
