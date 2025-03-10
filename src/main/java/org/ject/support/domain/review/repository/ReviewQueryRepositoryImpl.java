package org.ject.support.domain.review.repository;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.ject.support.common.data.RestPage;
import org.ject.support.domain.review.dto.QReviewResponse;
import org.ject.support.domain.review.dto.ReviewResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import static org.ject.support.domain.review.entity.QReview.review;

@Repository
@RequiredArgsConstructor
public class ReviewQueryRepositoryImpl implements ReviewQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public RestPage<ReviewResponse> findReviews(Pageable pageable) {
        List<ReviewResponse> content = queryFactory.select(new QReviewResponse(
                        review.id,
                        review.linkUrl,
                        review.title,
                        review.description,
                        review.summary))
                .from(review)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(review.createdAt.desc())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(review.count())
                .from(review);

        return new RestPage<>(content, pageable.getPageNumber(), pageable.getPageSize(), countQuery.fetchFirst());
    }
}
