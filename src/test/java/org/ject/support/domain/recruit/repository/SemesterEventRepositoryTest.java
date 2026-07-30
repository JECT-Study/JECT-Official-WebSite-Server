package org.ject.support.domain.recruit.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ject.support.domain.recruit.domain.SemesterEventType.EVENT;
import static org.ject.support.domain.recruit.domain.SemesterEventType.SURVEY;

import java.util.List;
import org.ject.support.domain.recruit.domain.SemesterEvent;
import org.ject.support.testconfig.QueryDslTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@Import(QueryDslTestConfig.class)
@DataJpaTest
class SemesterEventRepositoryTest {

    @Autowired
    private SemesterEventRepository semesterEventRepository;

    @Test
    @DisplayName("기수와 행사 유형으로 행사 목록을 등록 순서대로 조회한다")
    void 기수와_행사_유형으로_행사_목록을_등록_순서대로_조회한다() {
        // given
        SemesterEvent first = semesterEventRepository.save(SemesterEvent.create(4L, EVENT, "오리엔테이션"));
        SemesterEvent second = semesterEventRepository.save(SemesterEvent.create(4L, EVENT, "중간 발표"));
        semesterEventRepository.save(SemesterEvent.create(4L, SURVEY, "만족도 조사"));
        semesterEventRepository.save(SemesterEvent.create(5L, EVENT, "다른 기수 행사"));

        // when
        List<SemesterEvent> result = semesterEventRepository.findAllBySemesterIdAndTypeOrderByIdAsc(4L, EVENT);

        // then
        assertThat(result).containsExactly(first, second);
    }

}
