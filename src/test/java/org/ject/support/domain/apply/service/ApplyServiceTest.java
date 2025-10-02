package org.ject.support.domain.apply.service;

import org.ject.support.base.UnitTestSupport;
import org.ject.support.common.util.Map2JsonSerializer;
import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.repository.ApplicationFormRepository;
import org.ject.support.domain.apply.repository.ApplyRepository;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.repository.MemberRepository;
import org.ject.support.domain.recruit.domain.Question;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.exception.QuestionException;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.ject.support.domain.member.JobFamily.BE;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApplyServiceTest extends UnitTestSupport {

    @InjectMocks
    ApplyService applyService;

    @Mock
    MemberRepository memberRepository;

    @Mock
    RecruitRepository recruitRepository;

    @Mock
    ApplyRepository applyRepository;

    @Mock
    Map2JsonSerializer map2JsonSerializer;

    @Mock
    ApplicationFormRepository applicationFormRepository;

    @Test
    void 지원서_제출_성공() {
        // given
        List<Question> questions = List.of(
                getQuestion(1L, 1, "문항 1", "설명 1"),
                getQuestion(2L, 2, "문항 2", "설명 2"),
                getQuestion(3L, 3, "문항 3", "설명 3"),
                getQuestion(4L, 4, "문항 4", "설명 4"));

        Recruit recruit = getActiveRecruit(BE, questions);

        Member applicant = getApplicant();

        Map<String, String> answers = Map.of(
                "1", "답변 1",
                "2", "답변 2",
                "3", "답변 3",
                "4", "답변 4");

        Apply apply = getApply(recruit, applicant);

        String content = "answerToJson";

        when(memberRepository.findById(1L)).thenReturn(Optional.of(applicant));
        when(recruitRepository.findActiveRecruits(any())).thenReturn(List.of(recruit));
        when(applyRepository.findByMember(applicant)).thenReturn(Optional.of(apply));
        when(map2JsonSerializer.serializeAsString(answers)).thenReturn(content);

        // when
        applyService.submitApplication(1L, BE, answers, List.of());

        // then
        ArgumentCaptor<ApplicationForm> captor = ArgumentCaptor.forClass(ApplicationForm.class);
        verify(applicationFormRepository).save(captor.capture());

        ApplicationForm saved = captor.getValue();
        assertThat(saved.getApply()).isEqualTo(apply);
        assertThat(saved.getContent()).isEqualTo(content);
        assertThat(saved.getPortfolios()).isEmpty();
    }

    @Test
    void 지원서_제출_시_존재하지_않는_문항이_있으면_실패() {
        // given
        List<Question> questions = List.of(
                getQuestion(1L, 1, "문항 1", "설명 1"),
                getQuestion(2L, 2, "문항 2", "설명 2"),
                getQuestion(3L, 3, "문항 3", "설명 3"),
                getQuestion(5L, 5, "문항 5", "설명 5"));

        Recruit recruit = getActiveRecruit(BE, questions);

        Member applicant = getApplicant();

        Map<String, String> answers = Map.of(
                "1", "답변 1",
                "2", "답변 2",
                "3", "답변 3",
                "4", "답변 4");

        when(memberRepository.findById(1L)).thenReturn(Optional.of(applicant));
        when(recruitRepository.findActiveRecruits(any())).thenReturn(List.of(recruit));

        // when, then
        assertThatThrownBy(() -> applyService.submitApplication(1L, BE, answers, List.of()))
                .isInstanceOf(QuestionException.class);
    }

    @Test
    void 지원서를_제출한_지원자에_대한_제출_여부_확인_시_true_반환() {
        // given
        when(applicationFormRepository.existsByMemberId(any(), any())).thenReturn(true);

        // when
        boolean result = applyService.checkApplySubmit(1L);

        // then
        assertThat(result).isTrue();
    }

    private Recruit getActiveRecruit(JobFamily jobFamily, List<Question> questions) {
        return Recruit.builder()
                .id(1L)
                .semester(Semester.builder().id(1L).name("1기").build())
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .jobFamily(jobFamily)
                .questions(questions)
                .build();
    }

    private Question getQuestion(Long id, int sequence, String title, String label) {
        return Question.builder()
                .id(id)
                .sequence(sequence)
                .inputType(Question.InputType.TEXT)
                .isRequired(true)
                .title(title)
                .label(label)
                .build();
    }

    private Member getApplicant() {
        return Member.builder()
                .id(1L)
                .email("email@test.com")
                .pin("111111")
                .role(Role.APPLY)
                .status(MemberStatus.ACTIVE)
                .build();
    }

    private Apply getApply(Recruit recruit, Member applicant) {
        return Apply.builder()
                .id(1L)
                .recruit(recruit)
                .member(applicant)
                .status(Apply.Status.TEMP_SAVED)
                .build();
    }
}