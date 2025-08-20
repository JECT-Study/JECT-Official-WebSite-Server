package org.ject.support.domain.review.controller;

import lombok.RequiredArgsConstructor;
import org.ject.support.domain.review.dto.ReviewResponse;
import org.ject.support.domain.review.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController implements ReviewApiSpec {

    private final ReviewService reviewService;

    @Override
    @GetMapping
    public Page<ReviewResponse> findReviews(Pageable pageable) {
        return reviewService.findReviews(pageable);
    }
}
