package org.ject.support.admin.apply.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.ject.support.base.UnitTestSupport;
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
