package org.ject.support.admin.apply.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.ject.support.admin.apply.dto.AdminApplyDetailResponse;
import org.ject.support.admin.apply.dto.AdminApplyResponse;
import org.ject.support.admin.apply.dto.SubmittedApplyEditRequest;
import org.ject.support.admin.apply.repository.AdminApplyRepository;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.common.util.Map2JsonSerializer;
import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.apply.dto.ApplyPortfolioDto;
import org.ject.support.domain.apply.exception.ApplyErrorCode;
import org.ject.support.domain.apply.exception.ApplyException;
import org.ject.support.domain.apply.repository.ApplyRepository;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.recruit.domain.Question;
import org.ject.support.domain.recruit.domain.Recruit;
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
    private AdminApplyService submittedApplyService;

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
        var member = Member.builder()
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
                .semester(semester)
                .questions(List.of(question1, question2))
                .recruitType(org.ject.support.domain.recruit.domain.RecruitType.REGULAR)
                .build();

        submittedApply = Apply.builder()
                .id(applyId)
                .member(member)
                .recruit(recruit)
                .status(ApplyStatus.SUBMITTED)
                .note("Test note")
                .applicationForm(ApplicationForm.builder().build())
                .build();
    }

    @Test
    void 제출된_지원서_단건_삭제_성공() {
        // given
        var applyId = submittedApply.getId();
        given(adminApplyRepository.findByIdWithMember(applyId))
                .willReturn(Optional.of(submittedApply));

        // when
        submittedApplyService.deleteApply(applyId);

        // then
        verify(adminApplyRepository).findByIdWithMember(applyId);
        verify(adminApplyRepository).delete(submittedApply);
    }

    @Test
    void 제출된_지원서_단건_삭제시_존재하지_않으면_예외_발생() {
        // given
        var applyId = submittedApply.getId();
        given(adminApplyRepository.findByIdWithMember(applyId))
                .willReturn(Optional.empty());

        // expected
        assertThatThrownBy(() -> submittedApplyService.deleteApply(applyId))
                .isInstanceOf(ApplyException.class)
                .hasFieldOrPropertyWithValue("errorCode", ApplyErrorCode.NOT_FOUND_APPLY);
    }

    @Test
    void 제출된_지원서_여러건_삭제_성공() {
        // given
        var applyIds = List.of(1L, 2L, 3L);
        var member2 = Member.builder().name("김젝트2").build();
        var member3 = Member.builder().name("김젝트3").build();

        var apply2 = Apply.builder()
                .id(2L)
                .member(member2)
                .recruit(submittedApply.getRecruit())
                .status(ApplyStatus.SUBMITTED)
                .applicationForm(ApplicationForm.builder().build())
                .build();

        var apply3 = Apply.builder()
                .id(3L)
                .member(member3)
                .recruit(submittedApply.getRecruit())
                .status(ApplyStatus.SUBMITTED)
                .applicationForm(ApplicationForm.builder().build())
                .build();

        var applies = List.of(submittedApply, apply2, apply3);

        given(adminApplyRepository.findAllByIdWithMember(applyIds))
                .willReturn(applies);

        // when
        int deleted = submittedApplyService.deleteApplies(applyIds);

        // then
        verify(adminApplyRepository).findAllByIdWithMember(applyIds);
        assertThat(deleted).isEqualTo(3);
        verify(adminApplyRepository).deleteAll(applies);
    }

    @Test
    void 제출된_지원서_여러건_삭제시_일부가_존재하지_않으면_예외_발생() {
        // given
        var applyIds = List.of(1L, 2L, 3L);
        var applies = List.of(submittedApply); // 1개만 반환

        given(adminApplyRepository.findAllByIdWithMember(applyIds))
                .willReturn(applies);

        // expected
        assertThatThrownBy(() -> submittedApplyService.deleteApplies(applyIds))
                .isInstanceOf(ApplyException.class)
                .hasFieldOrPropertyWithValue("errorCode", ApplyErrorCode.NOT_FOUND_APPLY);
    }

    @Test
    void 제출된_지원서_여러건_삭제시_빈_리스트면_예외_발생() {
        // given
        var applyIds = List.of(1L, 2L, 3L);

        given(adminApplyRepository.findAllByIdWithMember(applyIds))
                .willReturn(List.of());

        // expected
        assertThatThrownBy(() -> submittedApplyService.deleteApplies(applyIds))
                .isInstanceOf(ApplyException.class)
                .hasFieldOrPropertyWithValue("errorCode", ApplyErrorCode.NOT_FOUND_APPLY);
    }

    @Test
    void 제출된_지원서_여러건_삭제시_중복_ID는_한_번만_처리() {
        // given
        var applyIds = List.of(1L, 1L, 1L);
        var distinctIds = List.of(1L);

        given(adminApplyRepository.findAllByIdWithMember(distinctIds))
                .willReturn(List.of(submittedApply));

        // when
        int deleted = submittedApplyService.deleteApplies(applyIds);

        // then
        assertThat(deleted).isEqualTo(1);
        verify(adminApplyRepository).findAllByIdWithMember(distinctIds);
    }

    @Test
    void 제출된_지원서_목록_조회_성공() {
        // given
        var pageable = PageRequest.of(0, 15);
        var jobFamily = JobFamily.BE;
        Long semesterId = null;

        var applies = List.of(submittedApply);
        var page = new PageImpl<>(applies, pageable, 1L);

        given(adminApplyRepository.findAppliesByStatus(ApplyStatus.SUBMITTED, semesterId, jobFamily, null, pageable))
                .willReturn(page);

        // when
        Page<AdminApplyResponse> result = submittedApplyService.findApplies(ApplyStatus.SUBMITTED, semesterId, jobFamily, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(adminApplyRepository).findAppliesByStatus(ApplyStatus.SUBMITTED, semesterId, jobFamily, null, pageable);
    }

    @Test
    void 제출된_지원서_목록_조회시_JobFamily가_null이면_전체_조회() {
        // given
        var pageable = PageRequest.of(0, 15);
        Long semesterId = null;
        var applies = List.of(submittedApply);
        var page = new PageImpl<>(applies, pageable, 1L);

        given(adminApplyRepository.findAppliesByStatus(ApplyStatus.SUBMITTED, semesterId, null, null, pageable))
                .willReturn(page);

        // when
        Page<AdminApplyResponse> result = submittedApplyService.findApplies(ApplyStatus.SUBMITTED, semesterId, null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(adminApplyRepository).findAppliesByStatus(ApplyStatus.SUBMITTED, semesterId, null, null, pageable);
    }

    @Test
    void 제출된_지원서_목록_조회시_결과가_없으면_빈_페이지_반환() {
        // given
        var pageable = PageRequest.of(0, 15);
        var jobFamily = JobFamily.BE;
        Long semesterId = null;
        var page = new PageImpl<Apply>(List.of(), pageable, 0L);

        given(adminApplyRepository.findAppliesByStatus(ApplyStatus.SUBMITTED, semesterId, jobFamily, null, pageable))
                .willReturn(page);

        // when
        Page<AdminApplyResponse> result = submittedApplyService.findApplies(ApplyStatus.SUBMITTED, semesterId, jobFamily, null, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        verify(adminApplyRepository).findAppliesByStatus(ApplyStatus.SUBMITTED, semesterId, jobFamily, null, pageable);
    }

    @Test
    void 제출된_지원서_목록_조회시_페이징_정보_정확히_전달() {
        // given
        var pageable = PageRequest.of(1, 10, Sort.by("createdAt").descending());
        var jobFamily = JobFamily.BE;
        Long semesterId = null;

        var member2 = Member.builder().name("김젝트2").build();
        var apply2 = Apply.builder()
                .id(2L)
                .member(member2)
                .recruit(submittedApply.getRecruit())
                .status(ApplyStatus.SUBMITTED)
                .applicationForm(ApplicationForm.builder().build())
                .build();

        var applies = List.of(submittedApply, apply2);
        var page = new PageImpl<>(applies, pageable, 25L);

        given(adminApplyRepository.findAppliesByStatus(ApplyStatus.SUBMITTED, semesterId, jobFamily, null, pageable))
                .willReturn(page);

        // when
        Page<AdminApplyResponse> result = submittedApplyService.findApplies(ApplyStatus.SUBMITTED, semesterId, jobFamily, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(25L);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(10);
        verify(adminApplyRepository).findAppliesByStatus(ApplyStatus.SUBMITTED, semesterId, jobFamily, null, pageable);
    }

    @Test
    void 제출된_지원서_목록을_semesterId로_필터링하여_조회() {
        // given
        var pageable = PageRequest.of(0, 15);
        var jobFamily = JobFamily.BE;
        Long semesterId = 1L;

        var applies = List.of(submittedApply);
        var page = new PageImpl<>(applies, pageable, 1L);

        given(adminApplyRepository.findAppliesByStatus(ApplyStatus.SUBMITTED, semesterId, jobFamily, null, pageable))
                .willReturn(page);

        // when
        Page<AdminApplyResponse> result = submittedApplyService.findApplies(ApplyStatus.SUBMITTED, semesterId, jobFamily, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(adminApplyRepository).findAppliesByStatus(ApplyStatus.SUBMITTED, semesterId, jobFamily, null, pageable);
    }

    @Test
    void 존재하지_않는_제출된_지원서를_상세조회할_경우_예외가_발생() {
        // given
        var applyId = submittedApply.getId() + 1L;
        given(adminApplyRepository.findApplyByIdByStatus(applyId, ApplyStatus.SUBMITTED))
                .willReturn(Optional.empty());

        // expected
        assertThatThrownBy(() -> submittedApplyService.findApply(applyId, ApplyStatus.SUBMITTED))
                .isInstanceOf(ApplyException.class)
                .hasFieldOrPropertyWithValue("errorCode", ApplyErrorCode.NOT_FOUND_APPLY);
    }

    @Test
    void 상세조회하려는_제출된_지원서의_ApplicationForm이_null이면_빈_응답을_반환() {
        // given
        var applyId = submittedApply.getId() + 1L;
        var member2 = Member.builder()
                .name("김젝트2")
                .build();
        var apply2 = Apply.builder()
                .id(applyId)
                .status(ApplyStatus.SUBMITTED)
                .applicationForm(null)
                .member(member2)
                .recruit(submittedApply.getRecruit())
                .build();

        given(adminApplyRepository.findApplyByIdByStatus(applyId, ApplyStatus.SUBMITTED))
                .willReturn(Optional.of(apply2));

        // when
        AdminApplyDetailResponse actual = submittedApplyService.findApply(applyId, ApplyStatus.SUBMITTED);

        // then
        assertThat(actual.applyId()).isEqualTo(applyId);
    }

    @Test
    void 제출된_지원서를_상세조회() {
        // given
        given(adminApplyRepository.findApplyByIdByStatus(
                submittedApply.getId(),
                ApplyStatus.SUBMITTED
        )).willReturn(Optional.of(submittedApply));

        // when
        AdminApplyDetailResponse actual = submittedApplyService.findApply(submittedApply.getId(), ApplyStatus.SUBMITTED);

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
    void 임시저장_상태의_지원서를_상세조회() {
        // given
        var tempApply = Apply.builder()
                .id(10L)
                .member(submittedApply.getMember())
                .recruit(submittedApply.getRecruit())
                .status(ApplyStatus.TEMP_SAVED)
                .note("")
                .applicationForm(ApplicationForm.builder().build())
                .build();

        given(adminApplyRepository.findApplyByIdByStatus(10L, ApplyStatus.TEMP_SAVED))
                .willReturn(Optional.of(tempApply));

        // when
        AdminApplyDetailResponse actual = submittedApplyService.findApply(10L, ApplyStatus.TEMP_SAVED);

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
                .member(submittedApply.getMember())
                .recruit(submittedApply.getRecruit())
                .status(ApplyStatus.REJECTED)
                .note("")
                .applicationForm(ApplicationForm.builder().build())
                .build();

        given(adminApplyRepository.findApplyByIdByStatus(20L, ApplyStatus.REJECTED))
                .willReturn(Optional.of(rejectedApply));

        // when
        AdminApplyDetailResponse actual = submittedApplyService.findApply(20L, ApplyStatus.REJECTED);

        // then
        verify(adminApplyRepository).findApplyByIdByStatus(20L, ApplyStatus.REJECTED);
        assertThat(actual.applyId()).isEqualTo(20L);
    }

    @Test
    void 합류_상태의_지원서를_상세조회() {
        // given
        var joinedApply = Apply.builder()
                .id(30L)
                .member(submittedApply.getMember())
                .recruit(submittedApply.getRecruit())
                .status(ApplyStatus.JOINED)
                .note("")
                .applicationForm(ApplicationForm.builder().build())
                .build();

        given(adminApplyRepository.findApplyByIdByStatus(30L, ApplyStatus.JOINED))
                .willReturn(Optional.of(joinedApply));

        // when
        AdminApplyDetailResponse actual = submittedApplyService.findApply(30L, ApplyStatus.JOINED);

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
        AdminApplyDetailResponse actual = submittedApplyService.findApply(submittedApply.getId(), null);

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
        assertThatThrownBy(() -> submittedApplyService.findApply(applyId, null))
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
        assertThatThrownBy(() -> submittedApplyService.findApply(applyId, ApplyStatus.TEMP_SAVED))
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
        assertThatThrownBy(() -> submittedApplyService.findApply(applyId, ApplyStatus.REJECTED))
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

        given(applyRepository.findByIdAndStatusWithMember(applyId, ApplyStatus.SUBMITTED))
                .willReturn(Optional.of(submittedApply));
        given(map2JsonSerializer.serializeAsString(newAnswers))
                .willReturn("{\"1\":\"수정된 답변1\",\"2\":\"수정된 답변2\"}");

        // when
        submittedApplyService.updateSubmittedApply(applyId, request);

        // then
        verify(applyRepository).findByIdAndStatusWithMember(applyId, ApplyStatus.SUBMITTED);
        verify(map2JsonSerializer).serializeAsString(newAnswers);
        assertThat(submittedApply.getMember().getName()).isEqualTo(newName);
        assertThat(submittedApply.getMember().getPhoneNumber()).isEqualTo(newPhoneNumber);
        assertThat(submittedApply.getMember().getEmail()).isEqualTo(newEmail);
        assertThat(submittedApply.getMember().getJobFamily()).isEqualTo(newJobFamily);
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

        given(applyRepository.findByIdAndStatusWithMember(applyId, ApplyStatus.SUBMITTED))
                .willReturn(Optional.empty());

        // expected
        assertThatThrownBy(() -> submittedApplyService.updateSubmittedApply(applyId, request))
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

        given(applyRepository.findByIdAndStatusWithMember(applyId, ApplyStatus.SUBMITTED))
                .willReturn(Optional.of(submittedApply));

        // expected
        assertThatThrownBy(() -> submittedApplyService.updateSubmittedApply(applyId, request))
                .isInstanceOf(QuestionException.class)
                .hasFieldOrPropertyWithValue("errorCode", QuestionErrorCode.NOT_FOUND_QUESTION);
    }

    @Test
    void 임시_저장된_지원서_목록_조회시_결과가_없으면_빈_페이지_반환() {
        // given
        var pageable = PageRequest.of(0, 10);
        Long semesterId = null;
        var page = new PageImpl<Apply>(List.of(), pageable, 0);

        given(adminApplyRepository.findAppliesByStatus(ApplyStatus.TEMP_SAVED, semesterId, null, null, pageable))
                .willReturn(page);

        // when
        Page<AdminApplyResponse> result = submittedApplyService.findApplies(ApplyStatus.TEMP_SAVED, semesterId, null, null, pageable);

        // then
        verify(adminApplyRepository).findAppliesByStatus(ApplyStatus.TEMP_SAVED, semesterId, null, null, pageable);
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void 임시_저장된_지원서_목록_조회_성공() {
        // given
        var pageable = PageRequest.of(0, 10);
        Long semesterId = null;

        var m1 = Member.builder().name("김1").jobFamily(JobFamily.BE).build();
        var m2 = Member.builder().name("김2").jobFamily(JobFamily.BE).build();
        var semester = Semester.builder().name("1").build();
        var recruit = Recruit.builder()
                .semester(semester)
                .recruitType(org.ject.support.domain.recruit.domain.RecruitType.REGULAR)
                .build();

        var a1 = Apply.builder()
                .id(1L)
                .member(m1)
                .recruit(recruit)
                .status(ApplyStatus.TEMP_SAVED)
                .applicationForm(ApplicationForm.builder().build())
                .build();

        var a2 = Apply.builder()
                .id(2L)
                .member(m2)
                .recruit(recruit)
                .status(ApplyStatus.TEMP_SAVED)
                .applicationForm(ApplicationForm.builder().build())
                .build();

        var applies = List.of(a1, a2);
        var page = new PageImpl<>(applies, pageable, applies.size());

        given(adminApplyRepository.findAppliesByStatus(ApplyStatus.TEMP_SAVED, semesterId, null, null, pageable))
                .willReturn(page);

        // when
        Page<AdminApplyResponse> result = submittedApplyService.findApplies(ApplyStatus.TEMP_SAVED, semesterId, null, null, pageable);

        // then
        verify(adminApplyRepository).findAppliesByStatus(ApplyStatus.TEMP_SAVED, semesterId, null, null, pageable);
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

        var member = Member.builder().name("김젝트").jobFamily(JobFamily.BE).build();
        var semester = Semester.builder().name("1").build();
        var recruit = Recruit.builder()
                .semester(semester)
                .recruitType(org.ject.support.domain.recruit.domain.RecruitType.REGULAR)
                .build();

        var apply = Apply.builder()
                .id(1L)
                .member(member)
                .recruit(recruit)
                .status(ApplyStatus.TEMP_SAVED)
                .applicationForm(ApplicationForm.builder().build())
                .build();

        var applies = List.of(apply);
        var page = new PageImpl<>(applies, pageable, applies.size());

        given(adminApplyRepository.findAppliesByStatus(ApplyStatus.TEMP_SAVED, semesterId, null, null, pageable))
                .willReturn(page);

        // when
        Page<AdminApplyResponse> result = submittedApplyService.findApplies(ApplyStatus.TEMP_SAVED, semesterId, null, null, pageable);

        // then
        verify(adminApplyRepository).findAppliesByStatus(ApplyStatus.TEMP_SAVED, semesterId, null, null, pageable);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().applyId()).isEqualTo(1L);
    }
}
