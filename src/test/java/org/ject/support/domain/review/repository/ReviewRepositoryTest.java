package org.ject.support.domain.review.repository;

import java.util.List;
import org.ject.support.domain.review.dto.ReviewResponse;
import org.ject.support.domain.review.entity.Review;
import org.ject.support.testconfig.QueryDslTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;

@Import(QueryDslTestConfig.class)
@DataJpaTest
class ReviewRepositoryTest {

    @Autowired
    ReviewRepository reviewRepository;

    @Test
    @DisplayName("리뷰 목록 조회")
    void find_reviews() {
        // given
        Review review1 = createReview();
        Review review2 = createReview();
        Review review3 = createReview();
        Review review4 = createReview();
        Review review5 = createReview();
        reviewRepository.saveAll(List.of(review1, review2, review3, review4, review5));

        // when
        Page<ReviewResponse> result = reviewRepository.findReviews(PageRequest.of(0, 4));

        // then
        assertThat(result.getContent()).hasSize(4);
        result.getContent().forEach(reviewResponse -> {
            assertThat(reviewResponse.id()).isNotNull();
            assertThat(reviewResponse.title()).isNotNull();
            assertThat(reviewResponse.linkUrl()).isNotNull();
            assertThat(reviewResponse.description()).isNotNull();
            assertThat(reviewResponse.summary()).isNotNull();
        });
    }

    private Review createReview() {
        return Review.builder()
                .linkUrl("https://test.com")
                .title("title")
                .description("description")
                .summary("summary")
                .build();
    }
}