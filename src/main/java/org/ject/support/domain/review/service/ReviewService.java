package org.ject.support.domain.review.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.domain.review.dto.ReviewResponse;
import org.ject.support.domain.review.repository.ReviewRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    @Cacheable(value = "review", key = "#pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<ReviewResponse> findReviews(Pageable pageable) {
        return reviewRepository.findReviews(pageable);
    }
}
