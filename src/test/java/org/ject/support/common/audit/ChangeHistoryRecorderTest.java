package org.ject.support.common.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.ject.support.base.UnitTestSupport;
import org.ject.support.domain.changehistory.domain.ChangeActionType;
import org.ject.support.domain.changehistory.domain.ChangeDetail;
import org.ject.support.domain.changehistory.domain.ChangeHistory;
import org.ject.support.domain.changehistory.repository.ChangeHistoryRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class ChangeHistoryRecorderTest extends UnitTestSupport {

    @InjectMocks
    private ChangeHistoryRecorder changeHistoryRecorder;

    @Mock
    private ChangeHistoryRepository changeHistoryRepository;

    @Test
    void 수집된_변경_상세가_없으면_이력을_저장하지_않는다() {
        // given
        AuditContext context = new AuditContext("MEMBER", ChangeActionType.UPDATE, "7", 10L);

        // when
        changeHistoryRecorder.record(context);

        // then
        verify(changeHistoryRepository, never()).save(any());
    }

    @Test
    void 수집된_변경_상세가_있으면_컨텍스트_정보로_이력을_저장한다() {
        // given
        AuditContext context = new AuditContext("MEMBER", ChangeActionType.UPDATE, "7", 10L);
        context.addDetail(ChangeDetail.create("Member", "7", ChangeActionType.UPDATE, "{}", "{}"));

        // when
        changeHistoryRecorder.record(context);

        // then
        ArgumentCaptor<ChangeHistory> captor = ArgumentCaptor.forClass(ChangeHistory.class);
        verify(changeHistoryRepository).save(captor.capture());

        ChangeHistory saved = captor.getValue();
        assertThat(saved.getDomain()).isEqualTo("MEMBER");
        assertThat(saved.getActionType()).isEqualTo(ChangeActionType.UPDATE);
        assertThat(saved.getTargetId()).isEqualTo("7");
        assertThat(saved.getActorId()).isEqualTo(10L);
        assertThat(saved.getChangeDetails()).hasSize(1);
    }
}
