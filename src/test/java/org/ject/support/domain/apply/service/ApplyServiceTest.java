package org.ject.support.domain.apply.service;

import org.ject.support.base.UnitTestSupport;
import org.ject.support.common.util.Map2JsonSerializer;
import org.ject.support.common.util.String2MapSerializer;
import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.dto.TempApplicationFormResponse;
import org.ject.support.domain.apply.exception.ApplyException;
import org.ject.support.domain.apply.repository.ApplicationFormRepository;
import org.ject.support.domain.apply.repository.ApplyRepository;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.entity.Member;
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
import static org.ject.support.domain.apply.domain.Apply.Status.JOINED;
import static org.ject.support.domain.apply.domain.Apply.Status.SUBMITTED;
import static org.ject.support.domain.apply.domain.Apply.Status.TEMP_SAVED;
import static org.ject.support.domain.member.JobFamily.BE;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApplyServiceTest extends UnitTestSupport {

    @InjectMocks
    ApplyService applyService;

    @Mock
    RecruitRepository recruitRepository;

    @Mock
    ApplyRepository applyRepository;

    @Mock
    Map2JsonSerializer map2JsonSerializer;

    @Mock
    ApplicationFormRepository applicationFormRepository;

    @Mock
    String2MapSerializer string2MapSerializer;

    @Test
    void 지원서_제출_성공() {
        // given
        List<Question> questions = List.of(
                getQuestion(1L, 1, "문항 1", "설명 1"),
                getQuestion(2L, 2, "문항 2", "설명 2"),
                getQuestion(3L, 3, "문항 3", "설명 3"),
                getQuestion(4L, 4, "문항 4", "설명 4"));

        Recruit recruit = getActiveRecruit(BE, questions);

        Member applicant = getApplicant(1L, "email@test.com");

        Map<String, String> answers = Map.of(
                "1", "답변 1",
                "2", "답변 2",
                "3", "답변 3",
                "4", "답변 4");

        Apply apply = getApply(recruit, applicant, TEMP_SAVED);

        String content = "answerToJson";

        when(recruitRepository.findActiveRecruits(any())).thenReturn(List.of(recruit));
        when(applyRepository.findByMemberId(applicant.getId())).thenReturn(Optional.of(apply));
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

        Map<String, String> answers = Map.of(
                "1", "답변 1",
                "2", "답변 2",
                "3", "답변 3",
                "4", "답변 4");

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

    @Test
    void 지원서_최초_임시저장_성공() {
        // given
        List<Question> questions = List.of(
                getQuestion(1L, 1, "문항 1", "설명 1"),
                getQuestion(2L, 2, "문항 2", "설명 2"),
                getQuestion(3L, 3, "문항 3", "설명 3"),
                getQuestion(4L, 4, "문항 4", "설명 4"));

        Recruit recruit = getActiveRecruit(BE, questions);

        Map<String, String> answers = Map.of(
                "1", "답변 1",
                "2", "답변 2",
                "3", "답변 3",
                "4", "답변 4");

        Member applicant = getApplicant(1L, "email@test.com");

        Apply apply = getApply(recruit, applicant, JOINED);

        String content = "newContent";

        when(applyRepository.findByMemberId(any())).thenReturn(Optional.of(apply));
        when(map2JsonSerializer.serializeAsString(answers)).thenReturn(content);

        // when
        applyService.saveApplicationTemporarily(1L, answers, List.of());

        // then
        ArgumentCaptor<ApplicationForm> captor = ArgumentCaptor.forClass(ApplicationForm.class);
        verify(applicationFormRepository).save(captor.capture());

        ApplicationForm saved = captor.getValue();
        assertThat(saved.getApply()).isEqualTo(apply);
        assertThat(saved.getContent()).isEqualTo(content);
        assertThat(saved.getPortfolios()).isEmpty();
        assertThat(apply.getApplicationForm()).isEqualTo(saved);
        assertThat(apply.getStatus()).isEqualTo(TEMP_SAVED);
    }

    @Test
    void 지원서를_이미_제출한_상태에서_임시저장_실패() {
        // given
        List<Question> questions = List.of(
                getQuestion(1L, 1, "문항 1", "설명 1"),
                getQuestion(2L, 2, "문항 2", "설명 2"),
                getQuestion(3L, 3, "문항 3", "설명 3"),
                getQuestion(4L, 4, "문항 4", "설명 4"));

        Recruit recruit = getActiveRecruit(BE, questions);

        Map<String, String> answers = Map.of(
                "1", "답변 1",
                "2", "답변 2",
                "3", "답변 3",
                "4", "답변 4");

        Member applicant = getApplicant(1L, "email@test.com");

        Apply apply = getApply(recruit, applicant, SUBMITTED);

        when(applyRepository.findByMemberId(any())).thenReturn(Optional.of(apply));

        // when, then
        assertThatThrownBy(() -> applyService.saveApplicationTemporarily(1L, answers, List.of()))
                .isInstanceOf(ApplyException.class);
    }

    @Test
    void 지원서를_임시저장한_상태에서_임시저장_성공() {
        // given
        List<Question> questions = List.of(
                getQuestion(1L, 1, "문항 1", "설명 1"),
                getQuestion(2L, 2, "문항 2", "설명 2"),
                getQuestion(3L, 3, "문항 3", "설명 3"),
                getQuestion(4L, 4, "문항 4", "설명 4"));

        Recruit recruit = getActiveRecruit(BE, questions);

        Map<String, String> answers = Map.of(
                "1", "답변 1",
                "2", "답변 2",
                "3", "답변 3",
                "4", "답변 4");

        Member applicant = getApplicant(1L, "email@test.com");

        ApplicationForm oldApplicationForm = ApplicationForm.builder()
                .id(1L)
                .content("oldContent")
                .build();

        Apply apply = Apply.builder()
                .id(1L)
                .recruit(recruit)
                .member(applicant)
                .applicationForm(oldApplicationForm)
                .status(TEMP_SAVED)
                .build();

        String newContent = "newContent";

        when(applyRepository.findByMemberId(any())).thenReturn(Optional.of(apply));
        when(map2JsonSerializer.serializeAsString(answers)).thenReturn(newContent);

        // when
        applyService.saveApplicationTemporarily(1L, answers, List.of());

        // then
        ApplicationForm newApplicationForm = apply.getApplicationForm();
        assertThat(newApplicationForm.getContent()).isEqualTo(newContent);
        assertThat(apply.getStatus()).isEqualTo(TEMP_SAVED);
    }

    @Test
    void 프로필과_임시저장한_지원서_제거_성공() {
        // given
        List<Question> questions = List.of(
                getQuestion(1L, 1, "문항 1", "설명 1"),
                getQuestion(2L, 2, "문항 2", "설명 2"),
                getQuestion(3L, 3, "문항 3", "설명 3"),
                getQuestion(4L, 4, "문항 4", "설명 4"));

        Recruit recruit = getActiveRecruit(BE, questions);

        ApplicationForm applicationForm = ApplicationForm.builder()
                .id(1L)
                .content("content")
                .build();

        Apply apply = Apply.builder()
                .id(1L)
                .recruit(recruit)
                .member(getApplicant(1L, "email@test.com"))
                .applicationForm(applicationForm)
                .status(TEMP_SAVED)
                .build();

        when(applyRepository.findByMemberId(any())).thenReturn(Optional.of(apply));

        // when
        applyService.deleteProfileAndTempApplicationForm(1L);

        // then
        assertThat(apply.getApplicationForm()).isNull();
        assertThat(apply.getStatus()).isEqualTo(JOINED);

        Member applicant = apply.getMember();
        assertThat(applicant.getName()).isNull();
        assertThat(applicant.getPhoneNumber()).isNull();
        assertThat(applicant.getJobFamily()).isNull();
        assertThat(applicant.getCareerDetails()).isNull();
        assertThat(applicant.getExperiencePeriod()).isNull();
        assertThat(applicant.getInterestedDomains()).isEmpty();
    }

    @Test
    void 임시저장한_지원서가_없거나_이미_제출한_상태에서_프로필과_임시저장한_지원서_제거_시_실패() {
        // given
        List<Question> questions = List.of(
                getQuestion(1L, 1, "문항 1", "설명 1"),
                getQuestion(2L, 2, "문항 2", "설명 2"),
                getQuestion(3L, 3, "문항 3", "설명 3"),
                getQuestion(4L, 4, "문항 4", "설명 4"));

        Recruit recruit = getActiveRecruit(BE, questions);

        Apply apply1 = Apply.builder()
                .id(1L)
                .recruit(recruit)
                .member(getApplicant(1L, "email1@test.com"))
                .applicationForm(null)
                .status(JOINED)
                .build();

        Apply apply2 = Apply.builder()
                .id(2L)
                .recruit(recruit)
                .member(getApplicant(2L, "email2@test.com"))
                .applicationForm(ApplicationForm.builder()
                        .id(2L)
                        .content("content")
                        .build())
                .status(SUBMITTED)
                .build();

        when(applyRepository.findByMemberId(1L)).thenReturn(Optional.of(apply1));
        when(applyRepository.findByMemberId(2L)).thenReturn(Optional.of(apply2));

        // when, then
        assertThatThrownBy(() -> applyService.deleteProfileAndTempApplicationForm(1L))
                .isInstanceOf(ApplyException.class);
        assertThatThrownBy(() -> applyService.deleteProfileAndTempApplicationForm(2L))
                .isInstanceOf(ApplyException.class);
    }

    @Test
    void 가장_최근에_임시저장한_지원서_조회_성공() {
        // given
        List<Question> questions = List.of(
                getQuestion(1L, 1, "문항 1", "설명 1"),
                getQuestion(2L, 2, "문항 2", "설명 2"),
                getQuestion(3L, 3, "문항 3", "설명 3"),
                getQuestion(4L, 4, "문항 4", "설명 4"));

        Recruit recruit = getActiveRecruit(BE, questions);

        Map<String, String> answers = Map.of("1", "answer1", "2", "answer2");
        ApplicationForm tempApplicationForm = ApplicationForm.builder()
                .id(1L)
                .content(answers.toString())
                .build();

        Apply apply = Apply.builder()
                .id(1L)
                .recruit(recruit)
                .member(getApplicant(1L, "email@test.com"))
                .applicationForm(tempApplicationForm)
                .status(TEMP_SAVED)
                .build();

        when(applyRepository.findByMemberId(any())).thenReturn(Optional.of(apply));
        when(string2MapSerializer.serializeAsMap(tempApplicationForm.getContent())).thenReturn(answers);

        // when
        TempApplicationFormResponse result = applyService.findTempApplicationForm(1L);

        // then
        assertThat(result.answers()).isEqualTo(answers);
        assertThat(result.portfolios()).isEmpty();
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

    private Member getApplicant(Long id, String email) {
        return Member.builder()
                .id(id)
                .email(email)
                .pin("111111")
                .role(Role.APPLY)
                .status(MemberStatus.ACTIVE)
                .build();
    }

    private Apply getApply(Recruit recruit, Member applicant, Apply.Status status) {
        return Apply.builder()
                .id(1L)
                .recruit(recruit)
                .member(applicant)
                .status(status)
                .build();
    }
}