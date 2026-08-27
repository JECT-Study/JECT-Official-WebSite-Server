package org.ject.support.admin.apply.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.ject.support.admin.apply.dto.AdminApplyDetailResponse;
import org.ject.support.admin.apply.dto.AdminApplyResponse;
import org.ject.support.admin.apply.dto.AdminApplySearchCondition;
import org.ject.support.admin.apply.dto.SelectionResultUpdateRequest;
import org.ject.support.admin.apply.dto.SubmittedApplyEditRequest;
import org.ject.support.admin.apply.repository.AdminApplyRepository;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.common.util.Map2JsonSerializer;
import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.apply.domain.Portfolio;
import org.ject.support.domain.apply.domain.SelectionResult;
import org.ject.support.domain.apply.dto.ApplyPortfolioDto;
import org.ject.support.domain.apply.exception.ApplyErrorCode;
import org.ject.support.domain.apply.exception.ApplyException;
import org.ject.support.domain.apply.repository.ApplyRepository;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.applicant.entity.Applicant;
import org.ject.support.domain.recruit.domain.Question;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.RecruitType;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.exception.QuestionErrorCode;
import org.ject.support.domain.recruit.exception.QuestionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

class AdminApplyServiceTest extends UnitTestSupport {

    @InjectMocks
    private AdminApplyService adminApplyService;

    @Mock
    private ApplyRepository applyRepository;

    @Mock
    private AdminApplyRepository adminApplyRepository;

    @Mock
    private Map2JsonSerializer map2JsonSerializer;

    private static Apply submittedApply;

    @BeforeEach
    void setUp() {
        var applyId = 1L;
        var member = Applicant.builder()
                .name("김젝트")
                .phoneNumber("010-1234-5678")
                .email("test@mail.com")
                .jobFamily(JobFamily.BE)
                .careerDetails(org.ject.support.domain.member.CareerDetails.STUDENT)
                .region(org.ject.support.domain.member.Region.SEOUL)
                .experiencePeriod(org.ject.support.domain.member.ExperiencePeriod.NONE)
                .interestedDomains(new java.util.ArrayList<>(List.of("AI", "Backend")))
                .build();
        var semester = Semester.builder()
                .name("1")
                .build();

        var question1 = Question.builder()
                .id(1L)
                .build();
        var question2 = Question.builder()
                .id(2L)
                .build();

        var recruit = Recruit.builder()
                .id(1L)
                .semester(semester)
                .questions(List.of(question1, question2))
                .recruitType(org.ject.support.domain.recruit.domain.RecruitType.REGULAR)
                .build();

        submittedApply = Apply.builder()
                .id(applyId)
                .applicant(member)
                .recruit(recruit)
                .status(ApplyStatus.SUBMITTED)
                .note("Test note")
                .applicationForm(ApplicationForm.builder().build())
                .build();
    }

    @Test
    void 제출된_지원들의_선정_결과를_일괄_변경한다() {
        // given
        var secondApply = Apply.builder()
                .id(2L)
                .applicant(Applicant.builder().name("김젝트2").build())
                .recruit(submittedApply.getRecruit())
                .status(ApplyStatus.SUBMITTED)
                .applicationForm(ApplicationForm.builder().build())
                .build();
        var request = new SelectionResultUpdateRequest(
                1L,
                List.of(
                        new SelectionResultUpdateRequest.SelectionResultItem(1L, SelectionResult.WAITLISTED, 1),
                        new SelectionResultUpdateRequest.SelectionResultItem(2L, SelectionResult.PASSED, null)
                )
        );
        given(adminApplyRepository.findAllByRecruitIdAndIdInWithApplicant(1L, List.of(1L, 2L)))
                .willReturn(List.of(submittedApply, secondApply));

        // when
        int updatedCount = adminApplyService.updateSelectionResults(request);

        // then
        assertThat(updatedCount).isEqualTo(2);
        assertThat(submittedApply.getSelectionResult()).isEqualTo(SelectionResult.WAITLISTED);
        assertThat(submittedApply.getWaitlistNumber()).isEqualTo(1);
        assertThat(secondApply.getSelectionResult()).isEqualTo(SelectionResult.PASSED);
        assertThat(secondApply.getWaitlistNumber()).isNull();
        verify(adminApplyRepository, times(2)).flush();
    }

    @Test
    void 기존_예비_번호를_서로_교체할_수_있다() {
        // given
        var secondApply = Apply.builder()
                .id(2L)
                .applicant(Applicant.builder().name("김젝트2").build())
                .recruit(submittedApply.getRecruit())
                .status(ApplyStatus.SUBMITTED)
                .applicationForm(ApplicationForm.builder().build())
                .build();
        submittedApply.decideSelectionResult(SelectionResult.WAITLISTED, 1);
        secondApply.decideSelectionResult(SelectionResult.WAITLISTED, 2);
        var request = new SelectionResultUpdateRequest(
                1L,
                List.of(
                        new SelectionResultUpdateRequest.SelectionResultItem(1L, SelectionResult.WAITLISTED, 2),
                        new SelectionResultUpdateRequest.SelectionResultItem(2L, SelectionResult.WAITLISTED, 1)
                )
        );
        given(adminApplyRepository.findAllByRecruitIdAndIdInWithApplicant(1L, List.of(1L, 2L)))
                .willReturn(List.of(submittedApply, secondApply));

        // when
        adminApplyService.updateSelectionResults(request);

        // then
        assertThat(submittedApply.getWaitlistNumber()).isEqualTo(2);
        assertThat(secondApply.getWaitlistNumber()).isEqualTo(1);
        verify(adminApplyRepository, times(2)).flush();
    }

    @Test
    void 모집_공고가_다르거나_존재하지_않는_지원이_포함되면_일괄_변경하지_않는다() {
        // given
        var request = new SelectionResultUpdateRequest(
                1L,
                List.of(new SelectionResultUpdateRequest.SelectionResultItem(999L, SelectionResult.PASSED, null))
        );
        given(adminApplyRepository.findAllByRecruitIdAndIdInWithApplicant(1L, List.of(999L)))
                .willReturn(List.of());

        // when & then
        assertThatThrownBy(() -> adminApplyService.updateSelectionResults(request))
                .isInstanceOf(ApplyException.class)
                .hasFieldOrPropertyWithValue("errorCode", ApplyErrorCode.NOT_FOUND_APPLY);
        assertThat(submittedApply.getSelectionResult()).isEqualTo(SelectionResult.UNDECIDED);
    }

    @Test
    void 요청에_같은_지원이_중복되면_일괄_변경하지_않는다() {
        // given
        var request = new SelectionResultUpdateRequest(
                1L,
                List.of(
                        new SelectionResultUpdateRequest.SelectionResultItem(1L, SelectionResult.PASSED, null),
                        new SelectionResultUpdateRequest.SelectionResultItem(1L, SelectionResult.FAILED, null)
                )
        );

        // when & then
        assertThatThrownBy(() -> adminApplyService.updateSelectionResults(request))
                .isInstanceOf(ApplyException.class)
                .hasFieldOrPropertyWithValue("errorCode", ApplyErrorCode.DUPLICATE_APPLY_ID);
        verifyNoInteractions(adminApplyRepository);
    }

    @Test
    void 같은_모집_공고에서_예비_번호가_중복되면_일괄_변경하지_않는다() {
        // given
        var request = new SelectionResultUpdateRequest(
                1L,
                List.of(
                        new SelectionResultUpdateRequest.SelectionResultItem(1L, SelectionResult.WAITLISTED, 1),
                        new SelectionResultUpdateRequest.SelectionResultItem(2L, SelectionResult.WAITLISTED, 1)
                )
        );

        // when & then
        assertThatThrownBy(() -> adminApplyService.updateSelectionResults(request))
                .isInstanceOf(ApplyException.class)
                .hasFieldOrPropertyWithValue("errorCode", ApplyErrorCode.DUPLICATE_WAITLIST_NUMBER);
        verifyNoInteractions(adminApplyRepository);
    }

    @Test
    void 제출되지_않은_지원이_하나라도_있으면_일괄_변경하지_않는다() {
        // given
        var tempApply = Apply.builder()
                .id(2L)
                .applicant(Applicant.builder().name("김젝트2").build())
                .recruit(submittedApply.getRecruit())
                .status(ApplyStatus.TEMP_SAVED)
                .applicationForm(ApplicationForm.builder().build())
                .build();
        var request = new SelectionResultUpdateRequest(
                1L,
                List.of(
                        new SelectionResultUpdateRequest.SelectionResultItem(1L, SelectionResult.PASSED, null),
                        new SelectionResultUpdateRequest.SelectionResultItem(2L, SelectionResult.PASSED, null)
                )
        );
        given(adminApplyRepository.findAllByRecruitIdAndIdInWithApplicant(1L, List.of(1L, 2L)))
                .willReturn(List.of(submittedApply, tempApply));

        // when & then
        assertThatThrownBy(() -> adminApplyService.updateSelectionResults(request))
                .isInstanceOf(ApplyException.class)
                .hasFieldOrPropertyWithValue("errorCode", ApplyErrorCode.NOT_SUBMITTED);
        assertThat(submittedApply.getSelectionResult()).isEqualTo(SelectionResult.UNDECIDED);
        verify(adminApplyRepository, never()).flush();
    }

    @Test
    void 단건_삭제_성공() {
        // given
        var applyId = submittedApply.getId();
        given(adminApplyRepository.findByIdWithApplicant(applyId))
                .willReturn(Optional.of(submittedApply));

        // when
        adminApplyService.deleteApply(applyId);

        // then
        verify(adminApplyRepository).findByIdWithApplicant(applyId);
        verify(adminApplyRepository).delete(submittedApply);
    }

    @Test
    void 단건_삭제시_존재하지_않으면_예외_발생() {
        // given
        var applyId = 999L;
        given(adminApplyRepository.findByIdWithApplicant(applyId))
                .willReturn(Optional.empty());

        // expected
        assertThatThrownBy(() -> adminApplyService.deleteApply(applyId))
                .isInstanceOf(ApplyException.class)
                .hasFieldOrPropertyWithValue("errorCode", ApplyErrorCode.NOT_FOUND_APPLY);
    }

    @Test
    void 다건_삭제하면_개수를_반환한다() {
        // given
        List<Long> applyIds = List.of(1L, 2L, 3L);
        doNothing().when(adminApplyRepository).deleteAllByIds(applyIds);

        // when
        int deletedCount = adminApplyService.deleteApplies(applyIds);

        // then
        assertThat(deletedCount).isEqualTo(applyIds.size());
        verify(adminApplyRepository).deleteAllByIds(applyIds);
    }

    @Test
    void 제출된_지원서_목록_조회_성공() {
        // given
        var pageable = PageRequest.of(0, 15);
        var jobFamily = JobFamily.BE;
        Long semesterId = null;

        var applies = List.of(submittedApply);
        var page = new PageImpl<>(applies, pageable, 1L);
        var condition = condition(ApplyStatus.SUBMITTED, semesterId, jobFamily, null);

        given(adminApplyRepository.findApplies(condition, pageable))
                .willReturn(page);

        // when
        Page<AdminApplyResponse> result = adminApplyService.findApplies(condition, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(adminApplyRepository).findApplies(condition, pageable);
    }

    @Test
    void 제출된_지원서_목록_조회시_JobFamily가_null이면_전체_조회() {
        // given
        var pageable = PageRequest.of(0, 15);
        Long semesterId = null;
        var applies = List.of(submittedApply);
        var page = new PageImpl<>(applies, pageable, 1L);
        var condition = condition(ApplyStatus.SUBMITTED, semesterId, null, null);

        given(adminApplyRepository.findApplies(condition, pageable))
                .willReturn(page);

        // when
        Page<AdminApplyResponse> result = adminApplyService.findApplies(condition, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(adminApplyRepository).findApplies(condition, pageable);
    }

    @Test
    void 제출된_지원서_목록_조회시_결과가_없으면_빈_페이지_반환() {
        // given
        var pageable = PageRequest.of(0, 15);
        var jobFamily = JobFamily.BE;
        Long semesterId = null;
        var page = new PageImpl<Apply>(List.of(), pageable, 0L);
        var condition = condition(ApplyStatus.SUBMITTED, semesterId, jobFamily, null);

        given(adminApplyRepository.findApplies(condition, pageable))
                .willReturn(page);

        // when
        Page<AdminApplyResponse> result = adminApplyService.findApplies(condition, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        verify(adminApplyRepository).findApplies(condition, pageable);
    }

    @Test
    void 제출된_지원서_목록_조회시_페이징_정보_정확히_전달() {
        // given
        var pageable = PageRequest.of(1, 10, Sort.by("createdAt").descending());
        var jobFamily = JobFamily.BE;
        Long semesterId = null;

        var member2 = Applicant.builder().name("김젝트2").build();
        var apply2 = Apply.builder()
                .id(2L)
                .applicant(member2)
                .recruit(submittedApply.getRecruit())
                .status(ApplyStatus.SUBMITTED)
                .applicationForm(ApplicationForm.builder().build())
                .build();

        var applies = List.of(submittedApply, apply2);
        var page = new PageImpl<>(applies, pageable, 25L);
        var condition = condition(ApplyStatus.SUBMITTED, semesterId, jobFamily, null);

        given(adminApplyRepository.findApplies(condition, pageable))
                .willReturn(page);

        // when
        Page<AdminApplyResponse> result = adminApplyService.findApplies(condition, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(25L);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(10);
        verify(adminApplyRepository).findApplies(condition, pageable);
    }

    @Test
    void 제출된_지원서_목록을_semesterId로_필터링하여_조회() {
        // given
        var pageable = PageRequest.of(0, 15);
        var jobFamily = JobFamily.BE;
        Long semesterId = 1L;

        var applies = List.of(submittedApply);
        var page = new PageImpl<>(applies, pageable, 1L);
        var condition = condition(ApplyStatus.SUBMITTED, semesterId, jobFamily, null);

        given(adminApplyRepository.findApplies(condition, pageable))
                .willReturn(page);

        // when
        Page<AdminApplyResponse> result = adminApplyService.findApplies(condition, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(adminApplyRepository).findApplies(condition, pageable);
    }

    @Test
    void 지원서_목록_조회시_공고_필터_조건을_전달한다() {
        // given
        var pageable = PageRequest.of(0, 15);
        var recruitId = 1L;
        var condition = condition(
                ApplyStatus.SUBMITTED, null, null, RecruitType.SEMESTER, RecruitTypeDetail.REFILL, recruitId);
        var applies = List.of(submittedApply);
        var page = new PageImpl<>(applies, pageable, 1L);

        given(adminApplyRepository.findApplies(condition, pageable))
                .willReturn(page);

        // when
        Page<AdminApplyResponse> result = adminApplyService.findApplies(condition, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        verify(adminApplyRepository).findApplies(condition, pageable);
    }

    @Test
    void 존재하지_않는_제출된_지원서를_상세조회할_경우_예외가_발생() {
        // given
        var applyId = submittedApply.getId() + 1L;
        given(adminApplyRepository.findApplyByIdByStatus(applyId, ApplyStatus.SUBMITTED))
                .willReturn(Optional.empty());

        // expected
        assertThatThrownBy(() -> adminApplyService.findApply(applyId, ApplyStatus.SUBMITTED))
                .isInstanceOf(ApplyException.class)
                .hasFieldOrPropertyWithValue("errorCode", ApplyErrorCode.NOT_FOUND_APPLY);
    }

    @Test
    void 상세조회하려는_제출된_지원서의_ApplicationForm이_null이면_빈_응답을_반환() {
        // given
        var applyId = submittedApply.getId() + 1L;
        var member2 = Applicant.builder()
                .name("김젝트2")
                .build();
        var apply2 = Apply.builder()
                .id(applyId)
                .status(ApplyStatus.SUBMITTED)
                .applicationForm(null)
                .applicant(member2)
                .recruit(submittedApply.getRecruit())
                .build();

        given(adminApplyRepository.findApplyByIdByStatus(applyId, ApplyStatus.SUBMITTED))
                .willReturn(Optional.of(apply2));

        // when
        AdminApplyDetailResponse actual = adminApplyService.findApply(applyId, ApplyStatus.SUBMITTED);

        // then
        assertThat(actual.applyId()).isEqualTo(applyId);
        assertThat(actual.portfolios()).isEmpty();
    }

    @Test
    void 제출된_지원서를_상세조회() {
        // given
        given(adminApplyRepository.findApplyByIdByStatus(
                submittedApply.getId(),
                ApplyStatus.SUBMITTED
        )).willReturn(Optional.of(submittedApply));

        // when
        AdminApplyDetailResponse actual = adminApplyService.findApply(submittedApply.getId(), ApplyStatus.SUBMITTED);

        // then
        verify(adminApplyRepository).findApplyByIdByStatus(submittedApply.getId(), ApplyStatus.SUBMITTED);
        assertThat(actual.applyId()).isEqualTo(submittedApply.getId());
        assertThat(actual.name()).isEqualTo("김젝트");
        assertThat(actual.phoneNumber()).isEqualTo("010-1234-5678");
        assertThat(actual.email()).isEqualTo("test@mail.com");
        assertThat(actual.jobFamily()).isEqualTo(JobFamily.BE);
        assertThat(actual.careerDetails()).isEqualTo("대학생(재학/휴학)");
        assertThat(actual.region()).isEqualTo("서울");
        assertThat(actual.experiencePeriod()).isEqualTo("경험 없음");
        assertThat(actual.interestedDomains()).containsExactly("AI", "Backend");
        assertThat(actual.recruitType()).isEqualTo("REGULAR");
        assertThat(actual.note()).isEqualTo("Test note");
    }

    @Test
    void 포트폴리오가_있는_지원서_상세조회_시_포트폴리오를_반환한다() {
        // given
        submittedApply.getApplicationForm().getPortfolios().addAll(List.of(
                Portfolio.builder()
                        .fileUrl("url1")
                        .fileName("name1")
                        .fileSize(100L)
                        .sequence(1)
                        .build(),
                Portfolio.builder()
                        .fileUrl("url2")
                        .fileName("name2")
                        .fileSize(200L)
                        .sequence(2)
                        .build()
        ));
        given(adminApplyRepository.findApplyByIdByStatus(
                submittedApply.getId(),
                ApplyStatus.SUBMITTED
        )).willReturn(Optional.of(submittedApply));

        // when
        AdminApplyDetailResponse actual = adminApplyService.findApply(
                submittedApply.getId(), ApplyStatus.SUBMITTED);

        // then
        assertThat(actual.portfolios()).containsExactly(
                new ApplyPortfolioDto("url1", "name1", "100", "1"),
                new ApplyPortfolioDto("url2", "name2", "200", "2")
        );
    }

    @Test
    void 임시저장_상태의_지원서를_상세조회() {
        // given
        var tempApply = Apply.builder()
                .id(10L)
                .applicant(submittedApply.getApplicant())
                .recruit(submittedApply.getRecruit())
                .status(ApplyStatus.TEMP_SAVED)
                .note("")
                .applicationForm(ApplicationForm.builder().build())
                .build();

        given(adminApplyRepository.findApplyByIdByStatus(10L, ApplyStatus.TEMP_SAVED))
                .willReturn(Optional.of(tempApply));

        // when
        AdminApplyDetailResponse actual = adminApplyService.findApply(10L, ApplyStatus.TEMP_SAVED);

        // then
        verify(adminApplyRepository).findApplyByIdByStatus(10L, ApplyStatus.TEMP_SAVED);
        assertThat(actual.applyId()).isEqualTo(10L);
        assertThat(actual.name()).isEqualTo("김젝트");
    }

    @Test
    void 거절_상태의_지원서를_상세조회() {
        // given
        var rejectedApply = Apply.builder()
                .id(20L)
                .applicant(submittedApply.getApplicant())
                .recruit(submittedApply.getRecruit())
                .status(ApplyStatus.REJECTED)
                .note("")
                .applicationForm(ApplicationForm.builder().build())
                .build();

        given(adminApplyRepository.findApplyByIdByStatus(20L, ApplyStatus.REJECTED))
                .willReturn(Optional.of(rejectedApply));

        // when
        AdminApplyDetailResponse actual = adminApplyService.findApply(20L, ApplyStatus.REJECTED);

        // then
        verify(adminApplyRepository).findApplyByIdByStatus(20L, ApplyStatus.REJECTED);
        assertThat(actual.applyId()).isEqualTo(20L);
    }

    @Test
    void 합류_상태의_지원서를_상세조회() {
        // given
        var joinedApply = Apply.builder()
                .id(30L)
                .applicant(submittedApply.getApplicant())
                .recruit(submittedApply.getRecruit())
                .status(ApplyStatus.JOINED)
                .note("")
                .applicationForm(ApplicationForm.builder().build())
                .build();

        given(adminApplyRepository.findApplyByIdByStatus(30L, ApplyStatus.JOINED))
                .willReturn(Optional.of(joinedApply));

        // when
        AdminApplyDetailResponse actual = adminApplyService.findApply(30L, ApplyStatus.JOINED);

        // then
        verify(adminApplyRepository).findApplyByIdByStatus(30L, ApplyStatus.JOINED);
        assertThat(actual.applyId()).isEqualTo(30L);
    }

    @Test
    void status가_null이면_상태와_무관하게_지원서를_상세조회() {
        // given
        given(adminApplyRepository.findApplyByIdByStatus(submittedApply.getId(), null))
                .willReturn(Optional.of(submittedApply));

        // when
        AdminApplyDetailResponse actual = adminApplyService.findApply(submittedApply.getId(), null);

        // then
        verify(adminApplyRepository).findApplyByIdByStatus(submittedApply.getId(), null);
        assertThat(actual.applyId()).isEqualTo(submittedApply.getId());
        assertThat(actual.name()).isEqualTo("김젝트");
    }

    @Test
    void status가_null이고_존재하지_않는_지원서를_상세조회할_경우_예외가_발생() {
        // given
        var applyId = 999L;
        given(adminApplyRepository.findApplyByIdByStatus(applyId, null))
                .willReturn(Optional.empty());

        // expected
        assertThatThrownBy(() -> adminApplyService.findApply(applyId, null))
                .isInstanceOf(ApplyException.class)
                .hasFieldOrPropertyWithValue("errorCode", ApplyErrorCode.NOT_FOUND_APPLY);
    }

    @Test
    void 임시저장_상태의_존재하지_않는_지원서를_상세조회할_경우_예외가_발생() {
        // given
        var applyId = 999L;
        given(adminApplyRepository.findApplyByIdByStatus(applyId, ApplyStatus.TEMP_SAVED))
                .willReturn(Optional.empty());

        // expected
        assertThatThrownBy(() -> adminApplyService.findApply(applyId, ApplyStatus.TEMP_SAVED))
                .isInstanceOf(ApplyException.class)
                .hasFieldOrPropertyWithValue("errorCode", ApplyErrorCode.NOT_FOUND_APPLY);
    }

    @Test
    void 거절_상태의_존재하지_않는_지원서를_상세조회할_경우_예외가_발생() {
        // given
        var applyId = 999L;
        given(adminApplyRepository.findApplyByIdByStatus(applyId, ApplyStatus.REJECTED))
                .willReturn(Optional.empty());

        // expected
        assertThatThrownBy(() -> adminApplyService.findApply(applyId, ApplyStatus.REJECTED))
                .isInstanceOf(ApplyException.class)
                .hasFieldOrPropertyWithValue("errorCode", ApplyErrorCode.NOT_FOUND_APPLY);
    }

    @Test
    void 제출된_지원서_수정_성공() {
        // given
        var applyId = submittedApply.getId();
        var newName = "수정된이름";
        var newPhoneNumber = "010-1234-5678";
        var newEmail = "updated@example.com";
        var newJobFamily = JobFamily.FE;

        var newAnswers = Map.of(
                "1", "수정된 답변1",
                "2", "수정된 답변2"
        );

        var newPortfolios = List.of(
                new ApplyPortfolioDto("url1", "name1", "1", "1"),
                new ApplyPortfolioDto("url2", "name2", "2", "2")
        );

        var request = new SubmittedApplyEditRequest(
                newName,
                newPhoneNumber,
                newEmail,
                newJobFamily,
                newAnswers,
                newPortfolios
        );

        given(adminApplyRepository.findByIdAndStatusWithApplicant(applyId, ApplyStatus.SUBMITTED))
                .willReturn(Optional.of(submittedApply));
        given(map2JsonSerializer.serializeAsString(newAnswers))
                .willReturn("{\"1\":\"수정된 답변1\",\"2\":\"수정된 답변2\"}");

        // when
        adminApplyService.updateSubmittedApply(applyId, request);

        // then
        verify(adminApplyRepository).findByIdAndStatusWithApplicant(applyId, ApplyStatus.SUBMITTED);
        verify(map2JsonSerializer).serializeAsString(newAnswers);
        assertThat(submittedApply.getApplicant().getName()).isEqualTo(newName);
        assertThat(submittedApply.getApplicant().getPhoneNumber()).isEqualTo(newPhoneNumber);
        assertThat(submittedApply.getApplicant().getEmail()).isEqualTo(newEmail);
        assertThat(submittedApply.getApplicant().getJobFamily()).isEqualTo(newJobFamily);
    }

    @Test
    void 제출된_지원서_수정시_존재하지_않으면_예외_발생() {
        // given
        var applyId = 999L;
        var request = new SubmittedApplyEditRequest(
                "이름",
                "01012345678",
                "test@example.com",
                JobFamily.BE,
                Map.of("1", "답변"),
                List.of()
        );

        given(adminApplyRepository.findByIdAndStatusWithApplicant(applyId, ApplyStatus.SUBMITTED))
                .willReturn(Optional.empty());

        // expected
        assertThatThrownBy(() -> adminApplyService.updateSubmittedApply(applyId, request))
                .isInstanceOf(ApplyException.class)
                .hasFieldOrPropertyWithValue("errorCode", ApplyErrorCode.NOT_FOUND_APPLY);
    }

    @Test
    void 제출된_지원서_수정시_유효하지_않은_질문ID면_예외_발생() {
        // given
        var applyId = submittedApply.getId();
        var invalidQuestionId = "999";

        var request = new SubmittedApplyEditRequest(
                "이름",
                "01012345678",
                "test@example.com",
                JobFamily.BE,
                Map.of(invalidQuestionId, "답변"),
                List.of()
        );

        given(adminApplyRepository.findByIdAndStatusWithApplicant(applyId, ApplyStatus.SUBMITTED))
                .willReturn(Optional.of(submittedApply));

        // expected
        assertThatThrownBy(() -> adminApplyService.updateSubmittedApply(applyId, request))
                .isInstanceOf(QuestionException.class)
                .hasFieldOrPropertyWithValue("errorCode", QuestionErrorCode.NOT_FOUND_QUESTION);
    }

    @Test
    void 임시_저장된_지원서_목록_조회시_결과가_없으면_빈_페이지_반환() {
        // given
        var pageable = PageRequest.of(0, 10);
        Long semesterId = null;
        var page = new PageImpl<Apply>(List.of(), pageable, 0);
        var condition = condition(ApplyStatus.TEMP_SAVED, semesterId, null, null);

        given(adminApplyRepository.findApplies(condition, pageable))
                .willReturn(page);

        // when
        Page<AdminApplyResponse> result = adminApplyService.findApplies(condition, pageable);

        // then
        verify(adminApplyRepository).findApplies(condition, pageable);
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void 임시_저장된_지원서_목록_조회_성공() {
        // given
        var pageable = PageRequest.of(0, 10);
        Long semesterId = null;

        var m1 = Applicant.builder().name("김1").jobFamily(JobFamily.BE).build();
        var m2 = Applicant.builder().name("김2").jobFamily(JobFamily.BE).build();
        var semester = Semester.builder().name("1").build();
        var recruit = Recruit.builder()
                .semester(semester)
                .recruitType(RecruitType.REGULAR)
                .build();

        var a1 = Apply.builder()
                .id(1L)
                .applicant(m1)
                .recruit(recruit)
                .status(ApplyStatus.TEMP_SAVED)
                .applicationForm(ApplicationForm.builder().build())
                .build();

        var a2 = Apply.builder()
                .id(2L)
                .applicant(m2)
                .recruit(recruit)
                .status(ApplyStatus.TEMP_SAVED)
                .applicationForm(ApplicationForm.builder().build())
                .build();

        var applies = List.of(a1, a2);
        var page = new PageImpl<>(applies, pageable, applies.size());
        var condition = condition(ApplyStatus.TEMP_SAVED, semesterId, null, null);

        given(adminApplyRepository.findApplies(condition, pageable))
                .willReturn(page);

        // when
        Page<AdminApplyResponse> result = adminApplyService.findApplies(condition, pageable);

        // then
        verify(adminApplyRepository).findApplies(condition, pageable);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).applyId()).isEqualTo(1L);
        assertThat(result.getContent().get(1).applyId()).isEqualTo(2L);
    }

    @Test
    void 임시_저장된_지원서_목록을_semesterId로_필터링하여_조회() {
        // given
        var pageable = PageRequest.of(0, 10);
        Long semesterId = 1L;

        var member = Applicant.builder().name("김젝트").jobFamily(JobFamily.BE).build();
        var semester = Semester.builder().name("1").build();
        var recruit = Recruit.builder()
                .semester(semester)
                .recruitType(RecruitType.REGULAR)
                .build();

        var apply = Apply.builder()
                .id(1L)
                .applicant(member)
                .recruit(recruit)
                .status(ApplyStatus.TEMP_SAVED)
                .applicationForm(ApplicationForm.builder().build())
                .build();

        var applies = List.of(apply);
        var page = new PageImpl<>(applies, pageable, applies.size());
        var condition = condition(ApplyStatus.TEMP_SAVED, semesterId, null, null);

        given(adminApplyRepository.findApplies(condition, pageable))
                .willReturn(page);

        // when
        Page<AdminApplyResponse> result = adminApplyService.findApplies(condition, pageable);

        // then
        verify(adminApplyRepository).findApplies(condition, pageable);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().applyId()).isEqualTo(1L);
    }

    private AdminApplySearchCondition condition(ApplyStatus applyStatus,
                                                Long semesterId,
                                                JobFamily jobFamily,
                                                RecruitType recruitType) {
        return condition(applyStatus, semesterId, jobFamily, recruitType, null, null);
    }

    private AdminApplySearchCondition condition(ApplyStatus applyStatus,
                                                Long semesterId,
                                                JobFamily jobFamily,
                                                RecruitType recruitType,
                                                RecruitTypeDetail recruitTypeDetail,
                                                Long recruitId) {
        return new AdminApplySearchCondition(
                applyStatus, semesterId, jobFamily, recruitType, recruitTypeDetail, recruitId);
    }
}
