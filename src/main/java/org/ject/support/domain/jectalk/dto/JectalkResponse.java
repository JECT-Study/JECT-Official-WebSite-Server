package org.ject.support.domain.jectalk.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;

@Builder
public record JectalkResponse(Long id, String name, String youtubeUrl, String imageUrl, String summary) {

    @QueryProjection
    public JectalkResponse {
    }
}
