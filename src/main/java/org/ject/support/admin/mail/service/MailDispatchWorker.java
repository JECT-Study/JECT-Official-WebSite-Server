package org.ject.support.admin.mail.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ject.support.admin.mail.domain.MailDispatchOutbox;
import org.ject.support.admin.mail.domain.MailDispatchOutboxStatus;
import org.ject.support.admin.mail.repository.MailDispatchOutboxRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailDispatchWorker {

    private static final int BATCH_SIZE = 50;
    private final MailDispatchOutboxRepository mailDispatchOutboxRepository;
    private final MailDispatchPersistenceService mailDispatchPersistenceService;
    private final MailDispatchDeliveryService mailDispatchDeliveryService;
    private final String workerId = UUID.randomUUID().toString();

    @Scheduled(fixedDelayString = "${mail.dispatch.worker.fixed-delay-ms:5000}")
    public void dispatchPending() {
        List<MailDispatchOutbox> candidates = mailDispatchOutboxRepository.findCandidates(
                List.of(MailDispatchOutboxStatus.PENDING, MailDispatchOutboxStatus.PROCESSING),
                PageRequest.of(0, BATCH_SIZE));
        candidates.forEach(this::dispatch);
    }

    private void dispatch(MailDispatchOutbox candidate) {
        try {
            mailDispatchPersistenceService.claimOutbox(candidate.getId(), workerId)
                    .ifPresent(claimed -> {
                        mailDispatchPersistenceService.startProcessing(claimed.getDispatchJob().getId());
                        mailDispatchDeliveryService.deliver(claimed);
                    });
        } catch (RuntimeException exception) {
            log.error("메일 Outbox 처리 실패 outboxId={}", candidate.getId(), exception);
        }
    }
}
