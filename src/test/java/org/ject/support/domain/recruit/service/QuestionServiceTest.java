package org.ject.support.domain.recruit.service;

import org.ject.support.base.UnitTestSupport;
import org.ject.support.domain.recruit.domain.Question;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.dto.QuestionResponse;
import org.ject.support.domain.recruit.dto.QuestionResponses;
import org.ject.support.domain.recruit.exception.RecruitException;
import org.ject.support.domain.recruit.repository.QuestionRepository;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.ject.support.domain.member.JobFamily.BE;
import static org.ject.support.domain.recruit.domain.Question.InputType.TEXT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class QuestionServiceTest extends UnitTestSupport {

    @InjectMocks
    QuestionService questionService;

    @Mock
    QuestionRepository questionRepository;

    @Mock
    RecruitRepository recruitRepository;

    @Test
    void 모집_공고_기준으로_지원서_문항을_조회한다() {
        // given
        Recruit recruit = createRecruit();
        QuestionResponse questionResponse = QuestionResponse.builder()
                .id(1L)
                .sequence(1)
                .inputType(TEXT)
                .isRequired(true)
                .title("title")
                .label("label")
                .build();
        when(recruitRepository.findActiveRecruitById(eq(1L), any(LocalDateTime.class))).thenReturn(recruit);
        when(questionRepository.findByRecruitIdOfActiveRecruit(any(LocalDateTime.class), eq(1L)))
                .thenReturn(List.of(questionResponse));

        // when
        QuestionResponses result = questionService.findQuestions(1L);

        // then
        assertThat(result.questionResponses()).hasSize(1);
        assertThat(result.questionResponses().get(0).title()).isEqualTo("title");
    }

    @Test
    void 모집_공고를_찾을_수_없으면_지원서_문항_조회에_실패한다() {
        // given
        when(recruitRepository.findActiveRecruitById(eq(1L), any(LocalDateTime.class))).thenReturn(null);

        // when, then
        assertThatThrownBy(() -> questionService.findQuestions(1L))
                .isInstanceOf(RecruitException.class);
        verify(questionRepository, never()).findByRecruitIdOfActiveRecruit(any(), any());
    }

    private Recruit createRecruit() {
        return Recruit.builder()
                .id(1L)
                .semester(Semester.builder().id(1L).name("1기").isRecruiting(true).build())
                .jobFamily(BE)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .questions(List.of(Question.builder()
                        .id(1L)
                        .sequence(1)
                        .inputType(TEXT)
                        .isRequired(true)
                        .title("title")
                        .label("label")
                        .build()))
                .build();
    }
}
