package org.ject.support.domain.admin.service;

import org.ject.support.base.UnitTestSupport;
import org.ject.support.common.util.String2MapSerializer;
import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.dto.TempApplyDetailResponse;
import org.ject.support.domain.apply.exception.ApplyException;
import org.ject.support.domain.apply.repository.ApplyRepository;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.Semester;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

class AdminTempApplyServiceTest extends UnitTestSupport {

    @InjectMocks
    private AdminTempApplyService adminTempApplyService;

    @Mock
    private ApplyRepository applyRepository;

    @Mock
    private String2MapSerializer string2MapSerializer;

    @Test
    @DisplayName("존재하지 않는 임시 저장된 지원서를 조회하면, 예외 발생")
    void getTempApplyDetail_fail() {
        // given
        Long tempApplyId = 1L;
        given(applyRepository.findByIdWithMember(tempApplyId))
                .willReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> adminTempApplyService.getTempApplyDetail(tempApplyId))
                .isInstanceOf(ApplyException.class);
    }

    @Test
    @DisplayName("상세 조회하려는 지원서의 상태가 임시저장 상태가 아닐 경우, 예외 발생")
    void getTempApplyDetail_fail2() {
        // given
        Long tempApplyId = 1L;
        Apply submittedApply = Apply.builder()
                .id(tempApplyId)
                .status(Apply.Status.SUBMITTED)
                .build();
        given(applyRepository.findByIdWithMember(tempApplyId))
                .willReturn(Optional.of(submittedApply));

        // when, then
        assertThatThrownBy(() -> adminTempApplyService.getTempApplyDetail(tempApplyId))
                .isInstanceOf(ApplyException.class);
    }

    @Test
    @DisplayName("임시 저장된 지원서를 상세 조회한다")
    void getTempApplyDetail() {
        // given
        Long tempApplyId = 1L;
        Member member = Member.builder()
                .name("김젝트")
                .build();
        Semester semester = Semester.builder()
                .name("1")
                .build();
        Recruit recruit = Recruit.builder()
                .semester(semester)
                .build();
        Apply submittedApply = Apply.builder()
                .id(tempApplyId)
                .member(member)
                .recruit(recruit)
                .status(Apply.Status.TEMP_SAVED)
                .applicationForm(ApplicationForm.builder().build())
                .build();
        given(applyRepository.findByIdWithMember(tempApplyId))
                .willReturn(Optional.of(submittedApply));

        // when
        TempApplyDetailResponse result = adminTempApplyService.getTempApplyDetail(tempApplyId);

        // then
        verify(applyRepository).findByIdWithMember(tempApplyId);
        assertThat(result.applyId()).isEqualTo(tempApplyId);
    }
}
