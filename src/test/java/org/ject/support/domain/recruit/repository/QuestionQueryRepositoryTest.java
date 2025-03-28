package org.ject.support.domain.recruit.repository;

import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.Question;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.dto.QuestionResponse;
import org.ject.support.testconfig.QueryDslTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ject.support.domain.member.JobFamily.BE;
import static org.ject.support.domain.member.JobFamily.FE;
import static org.ject.support.domain.recruit.domain.Question.InputType.FILE;
import static org.ject.support.domain.recruit.domain.Question.InputType.TEXT;

@Import(QueryDslTestConfig.class)
@DataJpaTest
class QuestionQueryRepositoryTest {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private RecruitRepository recruitRepository;

    @Test
    @DisplayName("현재 모집중인 직군의 지원서 문항 조회")
    void find_by_job_family() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Recruit feRecruit = createRecruit(now, FE);
        Recruit beRecruit = createRecruit(now, BE);
        recruitRepository.saveAll(List.of(feRecruit, beRecruit));

        Question feQuestion1 = createQuestion(1, TEXT, feRecruit);
        Question feQuestion2 = createQuestion(2, FILE, feRecruit);
        Question beQuestion1 = createQuestion(1, TEXT, beRecruit);
        Question beQuestion2 = createQuestion(2, TEXT, beRecruit);
        Question beQuestion3 = createQuestion(3, FILE, beRecruit);
        questionRepository.saveAll(List.of(feQuestion1, feQuestion2, beQuestion3, beQuestion1, beQuestion2));

        // when
        List<QuestionResponse> result = questionRepository.findByJobFamilyOfActiveRecruit(now, BE);

        // then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(QuestionResponse::sequence)
                .containsExactly(1, 2, 3);
    }

    private Recruit createRecruit(LocalDateTime now, JobFamily be) {
        return Recruit.builder()
                .semester("1기")
                .startDate(now.minusDays(1))
                .endDate(now.plusDays(1))
                .jobFamily(be)
                .build();
    }

    private Question createQuestion(int sequence, Question.InputType inputType, Recruit recruit) {
        return Question.builder()
                .sequence(sequence)
                .inputType(inputType)
                .isRequired(true)
                .title("title")
                .body("body")
                .inputHint("inputHint")
                .maxLength(500)
                .recruit(recruit)
                .build();
    }
}