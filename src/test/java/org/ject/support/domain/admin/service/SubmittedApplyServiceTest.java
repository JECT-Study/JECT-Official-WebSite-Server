package org.ject.support.domain.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import org.ject.support.common.util.String2MapSerializer;
import org.ject.support.domain.admin.dto.SubmittedApplyResponse;
import org.ject.support.domain.member.JobFamily;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.domain.admin.dto.SubmittedApplyCountResponse;
import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.Apply.Status;
import org.ject.support.domain.apply.exception.ApplyErrorCode;
import org.ject.support.domain.apply.exception.ApplyException;
import org.ject.support.domain.apply.repository.ApplyRepository;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.Semester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class SubmittedApplyServiceTest  extends UnitTestSupport {

    @InjectMocks
    private SubmittedApplyService submittedApplyService;

    @Mock
    private ApplyRepository applyRepository;

    @Mock
    private String2MapSerializer string2MapSerializer;

    private static Apply submittedApply;

    @BeforeEach
    void setUp() {
        var applyId = 1L;
        var member = Member.builder()
                .name("김젝트")
                .build();
        var semester = Semester.builder()
                .name("1")
                .build();
        var recruit = Recruit.builder()
                .semester(semester)
                .build();
        submittedApply = Apply.builder()
                .id(applyId)
                .member(member)
                .recruit(recruit)
                .status(Apply.Status.SUBMITTED)
                .applicationForm(ApplicationForm.builder().build())
                .build();
    }

    @Test
    void 제출된_지원서_단건_삭제_성공() {
        // given
        var applyId = submittedApply.getId();
        given(applyRepository.findByIdAndStatusWithMember(applyId, Status.SUBMITTED))
                .willReturn(Optional.of(submittedApply));

        // when
        submittedApplyService.deleteSubmittedApply(applyId);

        // then
        verify(applyRepository).findByIdAndStatusWithMember(applyId, Status.SUBMITTED);
        assertThat(submittedApply.getApplicationForm()).isNull();
        assertThat(submittedApply.getMember().getName()).isNull();
    }

    @Test
    void 제출된_지원서_단건_삭제시_존재하지_않으면_예외_발생() {
        // given
        var applyId = submittedApply.getId();
        given(applyRepository.findByIdAndStatusWithMember(applyId, Status.SUBMITTED))
                .willReturn(Optional.empty());

        // expected
        assertThatThrownBy(() -> submittedApplyService.deleteSubmittedApply(applyId))
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
                .status(Status.SUBMITTED)
                .applicationForm(ApplicationForm.builder().build())
                .build();

        var apply3 = Apply.builder()
                .id(3L)
                .member(member3)
                .recruit(submittedApply.getRecruit())
                .status(Status.SUBMITTED)
                .applicationForm(ApplicationForm.builder().build())
                .build();

        var applies = List.of(submittedApply, apply2, apply3);

        given(applyRepository.findAllByIdAndStatusWithMember(applyIds, Status.SUBMITTED))
                .willReturn(applies);

        // when
        submittedApplyService.deleteSubmittedApplies(applyIds);

        // then
        verify(applyRepository).findAllByIdAndStatusWithMember(applyIds, Status.SUBMITTED);
        assertThat(submittedApply.getApplicationForm()).isNull();
        assertThat(submittedApply.getMember().getName()).isNull();
        assertThat(apply2.getApplicationForm()).isNull();
        assertThat(apply3.getApplicationForm()).isNull();
    }

    @Test
    void 제출된_지원서_여러건_삭제시_일부가_존재하지_않으면_예외_발생() {
        // given
        var applyIds = List.of(1L, 2L, 3L);
        var applies = List.of(submittedApply); // 1개만 반환

        given(applyRepository.findAllByIdAndStatusWithMember(applyIds, Status.SUBMITTED))
                .willReturn(applies);

        // expected
        assertThatThrownBy(() -> submittedApplyService.deleteSubmittedApplies(applyIds))
                .isInstanceOf(ApplyException.class)
                .hasFieldOrPropertyWithValue("errorCode", ApplyErrorCode.NOT_FOUND_APPLY);
    }

    @Test
    void 제출된_지원서_여러건_삭제시_빈_리스트면_예외_발생() {
        // given
        var applyIds = List.of(1L, 2L, 3L);

        given(applyRepository.findAllByIdAndStatusWithMember(applyIds, Status.SUBMITTED))
                .willReturn(List.of());

        // expected
        assertThatThrownBy(() -> submittedApplyService.deleteSubmittedApplies(applyIds))
                .isInstanceOf(ApplyException.class)
                .hasFieldOrPropertyWithValue("errorCode", ApplyErrorCode.NOT_FOUND_APPLY);
    }

    @Test
    void 제출된_지원서_수_조회_성공() {
        // given
        Long expectedCount = 10L;
        given(applyRepository.countByStatus(Status.SUBMITTED))
                .willReturn(expectedCount);

        // when
        SubmittedApplyCountResponse response = submittedApplyService.countSubmittedApply();

        // then
        assertThat(response.count()).isEqualTo(expectedCount);
        then(applyRepository).should(times(1)).countByStatus(Status.SUBMITTED);
    }

    @Test
    void 제출된_지원서가_없을_때_0_반환() {
        // given
        Long expectedCount = 0L;
        given(applyRepository.countByStatus(Status.SUBMITTED))
                .willReturn(expectedCount);

        // when
        SubmittedApplyCountResponse response = submittedApplyService.countSubmittedApply();

        // then
        assertThat(response.count()).isZero();
        then(applyRepository).should(times(1)).countByStatus(Status.SUBMITTED);
    }

    @Test
    void 제출된_지원서_목록_조회_성공() {
        // given
        var pageable = PageRequest.of(0, 15);
        var jobFamily = JobFamily.BE;

        var applies = List.of(submittedApply);
        var page = new PageImpl<>(applies, pageable, 1L);

        given(applyRepository.findSubmittedApplies(jobFamily, pageable))
                .willReturn(page);

        // when
        Page<SubmittedApplyResponse> result = submittedApplyService.findSubmittedApplies(jobFamily, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(applyRepository).findSubmittedApplies(jobFamily, pageable);
    }

    @Test
    void 제출된_지원서_목록_조회시_JobFamily가_null이면_전체_조회() {
        // given
        var pageable = PageRequest.of(0, 15);
        var applies = List.of(submittedApply);
        var page = new PageImpl<>(applies, pageable, 1L);

        given(applyRepository.findSubmittedApplies(null, pageable))
                .willReturn(page);

        // when
        Page<SubmittedApplyResponse> result = submittedApplyService.findSubmittedApplies(null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(applyRepository).findSubmittedApplies(null, pageable);
    }

    @Test
    void 제출된_지원서_목록_조회시_결과가_없으면_빈_페이지_반환() {
        // given
        var pageable = PageRequest.of(0, 15);
        var jobFamily = JobFamily.BE;
        var page = new PageImpl<Apply>(List.of(), pageable, 0L);

        given(applyRepository.findSubmittedApplies(jobFamily, pageable))
                .willReturn(page);

        // when
        Page<SubmittedApplyResponse> result = submittedApplyService.findSubmittedApplies(jobFamily, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        verify(applyRepository).findSubmittedApplies(jobFamily, pageable);
    }

    @Test
    void 제출된_지원서_목록_조회시_페이징_정보_정확히_전달() {
        // given
        var pageable = PageRequest.of(1, 10, Sort.by("createdAt").descending());
        var jobFamily = JobFamily.BE;

        var member2 = Member.builder().name("김젝트2").build();
        var apply2 = Apply.builder()
                .id(2L)
                .member(member2)
                .recruit(submittedApply.getRecruit())
                .status(Status.SUBMITTED)
                .applicationForm(ApplicationForm.builder().build())
                .build();

        var applies = List.of(submittedApply, apply2);
        var page = new PageImpl<>(applies, pageable, 25L);

        given(applyRepository.findSubmittedApplies(jobFamily, pageable))
                .willReturn(page);

        // when
        Page<SubmittedApplyResponse> result = submittedApplyService.findSubmittedApplies(jobFamily, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(25L);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(10);
        verify(applyRepository).findSubmittedApplies(jobFamily, pageable);
    }

    @Test
    void 존재하지_않는_제출된_지원서를_상세조회할_경우_예외가_발생() {
        // given
        var applyId = submittedApply.getId() + 1L;
        given(applyRepository.findByIdAndStatusWithMember(applyId, Status.SUBMITTED))
                .willReturn(Optional.empty());

        // expected
        assertThatThrownBy(() -> submittedApplyService.findSubmittedApplyDetail(applyId))
                .isInstanceOf(ApplyException.class)
                .hasFieldOrPropertyWithValue("errorCode", ApplyErrorCode.NOT_FOUND_APPLY);
    }

    @Test
    void 상세조회하려는_제출된_지원서가_null인_경우_빈_응답을_반환() {
        // given
        var applyId = submittedApply.getId() + 1L;
        var member2 = Member.builder()
                .name("김젝트2")
                .build();
        var apply2 = Apply.builder()
                .id(applyId)
                .status(Status.SUBMITTED)
                .applicationForm(null)
                .member(member2)
                .recruit(submittedApply.getRecruit())
                .build();

        given(applyRepository.findByIdAndStatusWithMember(applyId, Status.SUBMITTED))
                .willReturn(Optional.of(apply2));

        // when
        var actual = submittedApplyService.findSubmittedApplyDetail(applyId);
        // expected
        assertThat(actual.applyId()).isEqualTo(applyId);
        assertThat(actual.applicationFormResponse().answers()).isEmpty();  // 빈 Map 확인
        assertThat(actual.applicationFormResponse().portfolios()).isEmpty();
    }

    @Test
    void 제출된_지원서를_상세조회() {
        // given
        given(applyRepository.findByIdAndStatusWithMember(
                submittedApply.getId(),
                Status.SUBMITTED
        )).willReturn(Optional.of(submittedApply));

        // when
        var actual = submittedApplyService.findSubmittedApplyDetail(submittedApply.getId());

        // then
        verify(applyRepository).findByIdAndStatusWithMember(
                submittedApply.getId(),
                Status.SUBMITTED
        );
        assertThat(actual.applyId()).isEqualTo(submittedApply.getId());
    }

}
