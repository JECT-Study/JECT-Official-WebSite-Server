package org.ject.support.domain.review.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.ject.support.common.springdoc.CustomApiResponse;
import org.ject.support.domain.review.dto.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Tag(name = "Review", description = "리뷰 API")
public interface ReviewApi {

    @Operation(
            summary = "리뷰 목록 조회",
            description = "리뷰 목록을 조회합니다.")
    @CustomApiResponse
    Page<ReviewResponse> findReviews(Pageable pageable);
}
