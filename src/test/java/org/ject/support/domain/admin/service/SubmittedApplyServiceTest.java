package org.ject.support.domain.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
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
}
