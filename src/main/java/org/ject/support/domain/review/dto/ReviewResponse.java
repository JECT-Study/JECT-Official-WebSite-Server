package org.ject.support.domain.review.dto;

import com.querydsl.core.annotations.QueryProjection;

public record ReviewResponse(Long id, String linkUrl, String title, String description, String summary) {

    @QueryProjection
    public ReviewResponse {
    }
}
