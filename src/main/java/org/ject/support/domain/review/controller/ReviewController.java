package org.ject.support.domain.review.controller;

import lombok.RequiredArgsConstructor;
import org.ject.support.common.data.RestPage;
import org.ject.support.domain.review.dto.ReviewResponse;
import org.ject.support.domain.review.service.ReviewService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public RestPage<ReviewResponse> findReviews(@PageableDefault(size = 4) Pageable pageable) {
        return reviewService.findReviews(pageable);
    }
}
