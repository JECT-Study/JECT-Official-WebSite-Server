package org.ject.support.admin.mail.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import org.ject.support.admin.mail.domain.MailDispatchJob;
import org.ject.support.admin.mail.domain.MailDispatchOutbox;
import org.ject.support.admin.mail.repository.MailDispatchOutboxRepository;
import org.ject.support.base.UnitTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

class MailDispatchWorkerTest extends UnitTestSupport {

    @Mock
    private MailDispatchOutboxRepository mailDispatchOutboxRepository;

    @Mock
    private MailDispatchPersistenceService mailDispatchPersistenceService;

    @Mock
    private MailDispatchDeliveryService mailDispatchDeliveryService;

    @InjectMocks
    private MailDispatchWorker mailDispatchWorker;

    @Test
    @DisplayName("대기 중인 Outbox를 claim한 뒤 발송한다")
    void 대기_중인_Outbox를_claim한_뒤_발송한다() {
        // given
        MailDispatchOutbox outbox = outbox();
        given(mailDispatchOutboxRepository.findCandidates(any(), any(), any(Pageable.class)))
                .willReturn(List.of(outbox));
        given(mailDispatchPersistenceService.claimOutbox(anyLong(), anyString()))
                .willReturn(Optional.of(outbox));

        // when
        mailDispatchWorker.dispatchPending();

        // then
        verify(mailDispatchPersistenceService).startProcessing(100L);
        verify(mailDispatchDeliveryService).deliver(outbox);
    }

    @Test
    @DisplayName("다른 worker가 먼저 claim한 Outbox는 발송하지 않는다")
    void 다른_worker가_먼저_claim한_Outbox는_발송하지_않는다() {
        // given
        MailDispatchOutbox outbox = outbox();
        given(mailDispatchOutboxRepository.findCandidates(any(), any(), any(Pageable.class)))
                .willReturn(List.of(outbox));
        given(mailDispatchPersistenceService.claimOutbox(anyLong(), anyString()))
                .willReturn(Optional.empty());

        // when
        mailDispatchWorker.dispatchPending();

        // then
        org.mockito.Mockito.verifyNoInteractions(mailDispatchDeliveryService);
    }

    private MailDispatchOutbox outbox() {
        MailDispatchJob job = MailDispatchJob.create(
                1L, 2L, 3L, "dispatch-key", "제목", "본문", "{}", 1);
        ReflectionTestUtils.setField(job, "id", 100L);
        MailDispatchOutbox outbox = MailDispatchOutbox.pending(
                job, 10L, "applicant@ject.kr", "제목", "본문");
        ReflectionTestUtils.setField(outbox, "id", 200L);
        return outbox;
    }
}
