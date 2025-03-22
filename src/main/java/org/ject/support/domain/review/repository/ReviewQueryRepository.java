package org.ject.support.domain.review.repository;

import org.ject.support.domain.review.dto.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewQueryRepository {

    Page<ReviewResponse> findReviews(Pageable pageable);
}
