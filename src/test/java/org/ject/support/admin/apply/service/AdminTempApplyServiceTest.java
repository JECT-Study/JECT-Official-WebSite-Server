package org.ject.support.admin.apply.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.ject.support.admin.apply.dto.TempApplyDetailResponse;
import org.ject.support.admin.apply.dto.TempSavedApplyCountResponse;
import org.ject.support.admin.apply.repository.AdminApplyQueryRepository;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.common.util.String2MapSerializer;
import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.apply.exception.ApplyException;
import org.ject.support.domain.apply.repository.ApplyRepository;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.Semester;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class AdminTempApplyServiceTest extends UnitTestSupport {

    @InjectMocks
    private AdminTempApplyService adminTempApplyService;

    @Mock
    private ApplyRepository applyRepository;

    @Mock
    private AdminApplyQueryRepository adminApplyQueryRepository;

    @Mock
    private String2MapSerializer string2MapSerializer;

    @Test
    void 존재하지_않는_임시_저장된_지원서를_조회하면_예외_발생() {
        // given
        Long tempApplyId = 1L;
        given(applyRepository.findByIdAndStatusWithMember(tempApplyId, ApplyStatus.TEMP_SAVED))
                .willReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> adminTempApplyService.getTempApplyDetail(tempApplyId))
                .isInstanceOf(ApplyException.class);
    }

    @Test
    void 임시_저장된_지원서를_상세_조회한다() {
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
                .status(ApplyStatus.TEMP_SAVED)
                .applicationForm(ApplicationForm.builder().build())
                .build();
        given(applyRepository.findByIdAndStatusWithMember(tempApplyId, ApplyStatus.TEMP_SAVED))
                .willReturn(Optional.of(submittedApply));

        // when
        TempApplyDetailResponse result = adminTempApplyService.getTempApplyDetail(tempApplyId);

        // then
        verify(applyRepository).findByIdAndStatusWithMember(tempApplyId, ApplyStatus.TEMP_SAVED);
        assertThat(result.applyId()).isEqualTo(tempApplyId);
    }

    @Test
    void 임시_저장된_지원서의_총_개수를_조회한다() {
        // given
        ApplyStatus targetStatus = ApplyStatus.TEMP_SAVED;
        Long tempSavedCount = 5L;
        given(applyRepository.countByStatus(targetStatus))
                .willReturn(tempSavedCount);

        // when
        TempSavedApplyCountResponse result = adminTempApplyService.getTempSavedApplyCount();

        // then
        verify(applyRepository).countByStatus(targetStatus);
        assertThat(result).isEqualTo(new TempSavedApplyCountResponse(tempSavedCount));
    }

    @Test
    void 임시_저장된_지원서의_총_개수가_0개일_경우_0을_조회한다() {
        // given
        ApplyStatus targetStatus = ApplyStatus.TEMP_SAVED;
        Long tempSavedCount = 0L;
        given(applyRepository.countByStatus(targetStatus))
                .willReturn(tempSavedCount);

        // when
        TempSavedApplyCountResponse result = adminTempApplyService.getTempSavedApplyCount();

        // then
        verify(applyRepository).countByStatus(targetStatus);
        assertThat(result).isEqualTo(new TempSavedApplyCountResponse(tempSavedCount));
    }

    @Test
    void 존재하지_않는_임시_저장_상태의_지원서를_삭제_할_경우_예외_발생() {
        // given
        Long tempApplyId = 1L;
        given(applyRepository.findByIdAndStatusWithMember(tempApplyId, ApplyStatus.TEMP_SAVED))
                .willReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> adminTempApplyService.deleteTempApply(tempApplyId))
                .isInstanceOf(ApplyException.class);
    }

    @Test
    void 임시_저장된_지원서를_삭제한다() {
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
        ApplicationForm applicationForm = ApplicationForm.builder()
                .content("{}")
                .build();
        Apply tempSavedApply = Apply.builder()
                .id(tempApplyId)
                .member(member)
                .recruit(recruit)
                .status(ApplyStatus.TEMP_SAVED)
                .applicationForm(applicationForm)
                .build();
        given(applyRepository.findByIdAndStatusWithMember(tempApplyId, ApplyStatus.TEMP_SAVED))
                .willReturn(Optional.of(tempSavedApply));

        // when
        adminTempApplyService.deleteTempApply(tempApplyId);

        // then
        verify(applyRepository).findByIdAndStatusWithMember(tempApplyId, ApplyStatus.TEMP_SAVED);
        verify(applyRepository).delete(tempSavedApply);
    }
}
