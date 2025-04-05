package org.ject.support.domain.recruit.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.ject.support.domain.member.JobFamily;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RecruitTest {

    @Test
    @DisplayName("is recruiting period")
    void is_recruiting_period() {
        // given
        Recruit recruit = Recruit.builder()
                .semesterId(1L)
                .jobFamily(JobFamily.BE)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .build();

        // when
        Boolean isRecruitingPeriod = recruit.isRecruitingPeriod();

        // then
        assertThat(isRecruitingPeriod).isTrue();
    }

    @Test
    @DisplayName("is invalid question semesterId")
    void is_invalid_question_id() {
        // given
        Recruit recruit = Recruit.builder()
                .semesterId(1L)
                .jobFamily(JobFamily.BE)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .build();

        Question question = Question.builder()
                .id(1L)
                .sequence(1)
                .inputType(Question.InputType.TEXT)
                .isRequired(true)
                .title("title")
                .recruit(recruit)
                .build();

        recruit.addQuestion(question);

        // when
        boolean isInvalidQuestionId = recruit.isInvalidQuestionId(2L);

        // then
        assertThat(isInvalidQuestionId).isTrue();
    }
}
