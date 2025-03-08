package org.ject.support.domain.review.repository;

import org.ject.support.common.data.RestPage;
import org.ject.support.domain.review.dto.ReviewResponse;
import org.springframework.data.domain.Pageable;

public interface ReviewQueryRepository {

    RestPage<ReviewResponse> findReviews(Pageable pageable);
}
