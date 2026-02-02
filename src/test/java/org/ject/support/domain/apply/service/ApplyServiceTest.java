package org.ject.support.domain.apply.service;

import org.ject.support.base.UnitTestSupport;
import org.ject.support.common.util.Map2JsonSerializer;
import org.ject.support.common.util.String2MapSerializer;
import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.dto.ApplyPortfolioDto;
import org.ject.support.domain.apply.dto.ApplyProfileRequest;
import org.ject.support.domain.apply.dto.ApplyStatusResponse;
import org.ject.support.domain.apply.dto.TempApplicationFormResponse;
import org.ject.support.domain.apply.exception.ApplyErrorCode;
import org.ject.support.domain.apply.exception.ApplyException;
import org.ject.support.domain.apply.repository.ApplicationFormRepository;
import org.ject.support.domain.apply.repository.ApplyRepository;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.InterestedDomain;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Region;
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
import org.springframework.context.ApplicationEventPublisher;

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
import static org.ject.support.domain.member.JobFamily.PD;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
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
    MemberRepository memberRepository;

    @Mock
    String2MapSerializer string2MapSerializer;

    @Mock
    ApplicationEventPublisher applicationEventPublisher;

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

        ApplicationForm applicationForm = getApplicationForm(answers.toString());

        Apply apply = getApply(1L, recruit, applicant, applicationForm, TEMP_SAVED);

        when(recruitRepository.findActiveRecruits(any())).thenReturn(List.of(recruit));
        when(applyRepository.findByMemberIdInActiveRecruit(eq(applicant.getId()), any())).thenReturn(Optional.of(apply));
        when(map2JsonSerializer.serializeAsString(answers)).thenReturn(answers.toString());

        // when
        applyService.submitApplication(1L, BE, answers, List.of());

        // then
        // 1. TEMP_SAVED 상태에서는 update가 발생하므로 save가 호출되지 않아야 함
        verify(applicationFormRepository, never()).save(any(ApplicationForm.class));

        // 2. 기존 applicationForm의 내용이 새로운 내용으로 업데이트되었는지 확인
        assertThat(applicationForm.getContent()).isEqualTo(answers.toString());
        assertThat(applicationForm.getPortfolios()).isEmpty();

        // 3. apply의 상태가 SUBMITTED로 변경되었는지 확인
        assertThat(apply.getStatus()).isEqualTo(SUBMITTED);
    }

    @Test
    void 지원서_제출_시_PD직군은_포트폴리오가_없으면_실패() {
        // given
        List<Question> questions = List.of(
                getQuestion(1L, 1, "문항 1", "설명 1"));

        Recruit recruit = getActiveRecruit(PD, questions);
        Member applicant = getApplicant(1L, "email@test.com");
        Map<String, String> answers = Map.of("1", "답변 1");
        ApplicationForm applicationForm = getApplicationForm(answers.toString());
        Apply apply = getApply(1L, recruit, applicant, applicationForm, TEMP_SAVED);

        when(recruitRepository.findActiveRecruits(any())).thenReturn(List.of(recruit));
        when(applyRepository.findByMemberIdInActiveRecruit(eq(applicant.getId()), any())).thenReturn(Optional.of(apply));
        when(map2JsonSerializer.serializeAsString(answers)).thenReturn(answers.toString());

        // expected
        assertThatThrownBy(() -> applyService.submitApplication(1L, PD, answers, List.of()))
                .isInstanceOf(ApplyException.class)
                .extracting("errorCode")
                .isEqualTo(ApplyErrorCode.PORTFOLIO_REQUIRED);
    }

    @Test
    void 지원서_제출_시_PD직군은_포트폴리오가_있으면_성공() {
        // given
        List<Question> questions = List.of(
                getQuestion(1L, 1, "문항 1", "설명 1"));

        Recruit recruit = getActiveRecruit(PD, questions);
        Member applicant = getApplicant(1L, "email@test.com");
        Map<String, String> answers = Map.of("1", "답변 1");
        ApplicationForm applicationForm = getApplicationForm(answers.toString());
        Apply apply = getApply(1L, recruit, applicant, applicationForm, TEMP_SAVED);

        List<ApplyPortfolioDto> portfolios = List.of(
                new ApplyPortfolioDto("url", "name", "100", "1")
        );

        when(recruitRepository.findActiveRecruits(any())).thenReturn(List.of(recruit));
        when(applyRepository.findByMemberIdInActiveRecruit(eq(applicant.getId()), any())).thenReturn(Optional.of(apply));
        when(map2JsonSerializer.serializeAsString(answers)).thenReturn(answers.toString());

        // when
        applyService.submitApplication(1L, PD, answers, portfolios);

        // then
        assertThat(apply.getStatus()).isEqualTo(SUBMITTED);
        assertThat(applicationForm.getPortfolios()).hasSize(1);
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
    void 지원상태_조회_시_프로필작성을_하지_않았을_경우_예외발생() {
        // given
        Member member = Member.builder()
                .id(1L)
                .name("지원자명")
                .phoneNumber("01012345678")
                .build();
        given(memberRepository.findById(member.getId()))
                .willReturn(Optional.of(member));
        given(applyRepository.findByMemberIdInActiveRecruit(eq(member.getId()), any()))
                .willReturn(Optional.empty());

        // expected
        assertThatThrownBy(() -> applyService.checkApplyStatus(member.getId()))
                .isInstanceOf(ApplyException.class)
                .extracting("errorCode")
                .isEqualTo(ApplyErrorCode.NOT_FOUND_APPLY);
    }

    @Test
    void 작성_중인_지원서가_있는_경우_TEMP_SAVED_반환() {
        // given
        Member member = Member.builder()
                .id(1L)
                .name("지원자명")
                .phoneNumber("01012345678")
                .build();
        given(memberRepository.findById(member.getId()))
                .willReturn(Optional.of(member));
        given(applyRepository.findByMemberIdInActiveRecruit(eq(member.getId()), any()))
                .willReturn(Optional.of(
                        Apply.builder()
                                .id(1L)
                                .status(TEMP_SAVED)
                                .build()
                ));

        // when
        ApplyStatusResponse result = applyService.checkApplyStatus(member.getId());

        // then
        assertThat(result.status()).isEqualTo(TEMP_SAVED);
    }

    @Test
    void 지원서를_제출한_지원자에_대한_제출_상태_확인_시_SUBMITTED_반환() {
        // given
        Member member = Member.builder()
                .id(1L)
                .name("지원자명")
                .phoneNumber("01012345678")
                .build();
        given(memberRepository.findById(member.getId()))
                .willReturn(Optional.of(member));
        given(applyRepository.findByMemberIdInActiveRecruit(eq(member.getId()), any()))
                .willReturn(Optional.of(
                        Apply.builder()
                                .id(1L)
                                .status(SUBMITTED)
                                .build()
                ));

        // when
        ApplyStatusResponse result = applyService.checkApplyStatus(member.getId());

        // then
        assertThat(result.status()).isEqualTo(SUBMITTED);
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

        ApplicationForm applicationForm = getApplicationForm(answers.toString());

        Apply apply = getApply(1L, recruit, applicant, applicationForm, JOINED);

        String content = "newContent";

        when(applyRepository.findByMemberIdInActiveRecruit(eq(applicant.getId()), any())).thenReturn(Optional.of(apply));
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

        ApplicationForm applicationForm = getApplicationForm(answers.toString());

        Apply apply = getApply(1L, recruit, applicant, applicationForm, SUBMITTED);

        when(applyRepository.findByMemberIdInActiveRecruit(eq(applicant.getId()), any())).thenReturn(Optional.of(apply));

        // when, then
        assertThatThrownBy(() -> applyService.saveApplicationTemporarily(1L, answers, List.of()))
                .isInstanceOf(ApplyException.class);
    }

    @Test
    void 프로필_저장_전에_임시저장_시도_시_실패() {
        // given
        long memberId = 1L;
        Map<String, String> answers = Map.of("1", "답변1");

        given(applyRepository.findByMemberIdInActiveRecruit(eq(memberId), any())).willReturn(Optional.empty());

        // expected
        assertThatThrownBy(() -> applyService.saveApplicationTemporarily(memberId, answers, List.of()))
                .isInstanceOf(ApplyException.class)
                .extracting("errorCode")
                .isEqualTo(ApplyErrorCode.NOT_FOUND_APPLY);
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

        Apply apply = getApply(1L, recruit, applicant, oldApplicationForm, TEMP_SAVED);

        String newContent = "newContent";

        when(applyRepository.findByMemberIdInActiveRecruit(eq(applicant.getId()), any())).thenReturn(Optional.of(apply));
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

        Apply apply = getApply(1L, recruit, getApplicant(1L, "email@test.com"), applicationForm, TEMP_SAVED);

        when(applyRepository.findByMemberIdInActiveRecruit(eq(1L), any())).thenReturn(Optional.of(apply));

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

        Apply apply1 = getApply(1L, recruit, getApplicant(1L, "email1@test.com"), null, JOINED);
        Apply apply2 = getApply(2L, recruit, getApplicant(2L, "email2@test.com"), ApplicationForm.builder()
                .id(2L)
                .content("content")
                .build(), SUBMITTED);

        when(applyRepository.findByMemberIdInActiveRecruit(eq(1L), any())).thenReturn(Optional.of(apply1));
        when(applyRepository.findByMemberIdInActiveRecruit(eq(2L), any())).thenReturn(Optional.of(apply2));

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
        ApplicationForm tempApplicationForm = getApplicationForm(answers.toString());

        Apply apply = getApply(1L, recruit, getApplicant(1L, "email@test.com"), tempApplicationForm, TEMP_SAVED);

        when(applyRepository.findByMemberIdInActiveRecruit(eq(1L), any())).thenReturn(Optional.of(apply));
        when(string2MapSerializer.serializeAsMap(tempApplicationForm.getContent())).thenReturn(answers);

        // when
        TempApplicationFormResponse result = applyService.findTempApplicationForm(1L);

        // then
        assertThat(result.answers()).isEqualTo(answers);
        assertThat(result.portfolios()).isEmpty();
        assertThat(result.jobFamily()).isEqualTo(BE);
    }

    @Test
    void 임시저장한_지원서가_없는_상태에서_조회_시_실패() {
        // given
        List<Question> questions = List.of(
                getQuestion(1L, 1, "문항 1", "설명 1"),
                getQuestion(2L, 2, "문항 2", "설명 2"),
                getQuestion(3L, 3, "문항 3", "설명 3"),
                getQuestion(4L, 4, "문항 4", "설명 4"));

        Apply apply = getApply(1L, getActiveRecruit(BE, questions), getApplicant(1L, "email@test.com"), null, JOINED);

        when(applyRepository.findByMemberIdInActiveRecruit(eq(1L), any())).thenReturn(Optional.of(apply));

        // when, then
        assertThatThrownBy(() -> applyService.findTempApplicationForm(1L))
                .isInstanceOf(ApplyException.class);
    }

    @Test
    void 프로필_저장_성공() {
        // given
        long memberId = 1L;
        Member member = getApplicant(memberId, "test@example.com");
        ApplyProfileRequest request = new ApplyProfileRequest(
            "New Name",
            "010-1234-5678",
            JobFamily.FE,
            Region.SEOUL,
            CareerDetails.STUDENT,
            ExperiencePeriod.NONE,
            List.of(InterestedDomain.GAME.getDescription(), InterestedDomain.EDUCATION.getDescription())
        );
        Recruit recruit = getActiveRecruit(request.jobFamily(), List.of());

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(applyRepository.existsByMemberIdInActiveRecruit(eq(memberId), any())).willReturn(false);
        given(recruitRepository.findActiveRecruits(any())).willReturn(List.of(recruit));

        // when
        applyService.saveProfile(memberId, request);

        // then
        ArgumentCaptor<Apply> applyCaptor = ArgumentCaptor.forClass(Apply.class);
        verify(applyRepository).save(applyCaptor.capture());
        Apply savedApply = applyCaptor.getValue();

        assertThat(savedApply.getMember()).isEqualTo(member);
        assertThat(savedApply.getRecruit()).isEqualTo(recruit);
        assertThat(savedApply.getStatus()).isEqualTo(JOINED);

        assertThat(member.getName()).isEqualTo(request.name());
        assertThat(member.getPhoneNumber()).isEqualTo(request.phoneNumber());
        assertThat(member.getJobFamily()).isEqualTo(request.jobFamily());
    }

    @Test
    void 프로필_저장_시_Apply가_존재하면_프로필만_업데이트() {
        // given
        long memberId = 1L;
        Member member = getApplicant(memberId, "test@example.com");
        ApplyProfileRequest request = new ApplyProfileRequest(
                "New Name",
                "010-1234-5678",
                JobFamily.FE,
                Region.SEOUL,
                CareerDetails.STUDENT,
                ExperiencePeriod.NONE,
                List.of(InterestedDomain.GAME.getDescription(), InterestedDomain.EDUCATION.getDescription())
        );

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(applyRepository.existsByMemberIdInActiveRecruit(eq(memberId), any())).willReturn(true);

        // when
        applyService.saveProfile(memberId, request);

        // then
        verify(applyRepository, never()).save(any(Apply.class));

        assertThat(member.getName()).isEqualTo(request.name());
        assertThat(member.getPhoneNumber()).isEqualTo(request.phoneNumber());
        assertThat(member.getJobFamily()).isEqualTo(request.jobFamily());
        assertThat(member.getCareerDetails()).isEqualTo(request.careerDetails());
        assertThat(member.getExperiencePeriod()).isEqualTo(request.experiencePeriod());
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

    private Apply getApply(Long id, Recruit recruit, Member applicant, ApplicationForm applicationForm, Apply.Status status) {
        return Apply.builder()
                .id(id)
                .recruit(recruit)
                .member(applicant)
                .applicationForm(applicationForm)
                .status(status)
                .build();
    }

    private ApplicationForm getApplicationForm(String content) {
        return ApplicationForm.builder()
                .id(1L)
                .content(content)
                .build();
    }
}
