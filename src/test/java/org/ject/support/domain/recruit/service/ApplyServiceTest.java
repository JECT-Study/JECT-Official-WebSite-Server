package org.ject.support.domain.recruit.service;

import org.assertj.core.api.Assertions;
import org.ject.support.domain.recruit.exception.ApplyException;
import org.ject.support.domain.tempapply.service.TemporaryApplyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.ject.support.domain.member.JobFamily.FE;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplyServiceTest {

    @InjectMocks
    ApplyService applyService;

    @Mock
    TemporaryApplyService temporaryApplyService;

    @Test
    @DisplayName("지원서 직군 변경")
    void change_job_family() {
        // given
        when(temporaryApplyService.hasSameJobFamilyWithRecentTemporaryApplication(1L, FE)).thenReturn(false);

        // when
        applyService.changeJobFamily(1L, FE);

        // then
        verify(temporaryApplyService).deleteTemporaryApplicationsByMemberId(1L);
    }

    @Test
    @DisplayName("변경 요청한 직군이 이전에 임시 저장한 지원서의 직군과 동일한 경우 실패")
    void change_job_family_Fail_by_duplicate_job_family() {
        // given
        when(temporaryApplyService.hasSameJobFamilyWithRecentTemporaryApplication(1L, FE)).thenReturn(true);

        // when, then
        Assertions.assertThatThrownBy(() -> applyService.changeJobFamily(1L, FE))
                .isInstanceOf(ApplyException.class);
    }
}