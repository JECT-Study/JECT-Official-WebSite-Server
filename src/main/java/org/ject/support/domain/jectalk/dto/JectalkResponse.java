package org.ject.support.domain.jectalk.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import org.ject.support.domain.jectalk.enums.ContentType;

@Builder
public record JectalkResponse(
        Long id,
        String title,
        String description,
        String contentUrl,
        ContentType contentType,
        String thumbnailUrl,
        String summary) {

    @QueryProjection
    public JectalkResponse {
    }
}
