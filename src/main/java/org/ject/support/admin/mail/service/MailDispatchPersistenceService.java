package org.ject.support.admin.mail.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.mail.domain.MailDispatchJob;
import org.ject.support.admin.mail.domain.MailDispatchOutbox;
import org.ject.support.admin.mail.domain.MailDispatchTarget;
import org.ject.support.admin.mail.dto.MailDispatchResponse;
import org.ject.support.admin.mail.exception.MailErrorCode;
import org.ject.support.admin.mail.exception.MailException;
import org.ject.support.admin.mail.repository.MailDispatchJobRepository;
import org.ject.support.admin.mail.repository.MailDispatchOutboxRepository;
import org.ject.support.admin.mail.repository.MailDispatchTargetRepository;
import org.ject.support.common.util.Map2JsonSerializer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MailDispatchPersistenceService {

    private final MailDispatchJobRepository mailDispatchJobRepository;
    private final MailDispatchTargetRepository mailDispatchTargetRepository;
    private final MailDispatchOutboxRepository mailDispatchOutboxRepository;
    private final Map2JsonSerializer map2JsonSerializer;

    @Transactional
    public MailDispatchJob createJob(MailDispatchPlan plan, String requestFingerprint) {
        return persistJob(plan, requestFingerprint, null);
    }

    @Transactional
    public MailDispatchJob createScheduledJob(MailDispatchPlan plan, String requestFingerprint, Instant scheduledAt) {
        if (scheduledAt == null) {
            throw new MailException(MailErrorCode.INVALID_SCHEDULED_AT);
        }
        if (plan.targets().stream().anyMatch(target -> target.selectionResultSnapshot() == null
                || target.selectionResultSnapshot().isBlank())) {
            throw new MailException(MailErrorCode.INVALID_DISPATCH_TARGETS);
        }
        return persistJob(plan, requestFingerprint, scheduledAt);
    }

    private MailDispatchJob persistJob(MailDispatchPlan plan, String requestFingerprint, Instant scheduledAt) {
        MailDispatchJob job = MailDispatchJob.create(
                plan.scenarioId(),
                plan.recruitId(),
                plan.requestedByAdminId(),
                plan.idempotencyKey(),
                plan.subjectTemplate(),
                plan.bodyTemplate(),
                map2JsonSerializer.serializeAsString(plan.inputVariables()),
                requestFingerprint,
                plan.targets().size()
        );
        if (scheduledAt != null) {
            job.schedule(scheduledAt);
        }
        MailDispatchJob savedJob = mailDispatchJobRepository.save(job);
        List<MailDispatchTarget> targets = plan.targets().stream()
                .map(target -> scheduledAt == null
                        ? MailDispatchTarget.pending(savedJob, target.applyId(), target.email())
                        : MailDispatchTarget.pending(savedJob, target.applyId(), target.email(),
                                target.selectionResultSnapshot()))
                .toList();
        mailDispatchTargetRepository.saveAll(targets);
        // 발송 전 결과 snapshot을 작업·대상과 같은 트랜잭션에 보존한다.
        List<MailDispatchOutbox> outboxes = plan.targets().stream()
                .map(target -> MailDispatchOutbox.createPending(
                        savedJob, target.applyId(), target.email(), target.subject(), target.body()))
                .toList();
        mailDispatchOutboxRepository.saveAll(outboxes);
        return savedJob;
    }

    @Transactional
    public void startProcessing(Long dispatchJobId) {
        MailDispatchJob job = findJob(dispatchJobId);
        job.startProcessing();
    }

    @Transactional
    public void recordSuccess(Long dispatchJobId, Long applyId) {
        MailDispatchJob job = findJob(dispatchJobId);
        MailDispatchTarget target = findTarget(dispatchJobId, applyId);
        target.markSent();
        job.recordSuccess();
        // Outbox 도입 전 작업의 결과 저장도 유지한다.
        mailDispatchOutboxRepository.findByDispatchJobIdAndApplyId(dispatchJobId, applyId)
                .ifPresent(MailDispatchOutbox::markSent);
    }

    @Transactional
    public void recordFailure(Long dispatchJobId, Long applyId, String failureReason) {
        MailDispatchJob job = findJob(dispatchJobId);
        MailDispatchTarget target = findTarget(dispatchJobId, applyId);
        target.markFailed(failureReason);
        job.recordFailure();
        // Outbox 도입 전 작업의 결과 저장도 유지한다.
        mailDispatchOutboxRepository.findByDispatchJobIdAndApplyId(dispatchJobId, applyId)
                .ifPresent(outbox -> outbox.markFailed(failureReason));
    }

    @Transactional(readOnly = true)
    public Optional<MailDispatchJob> findJobByIdempotencyKey(Long requestedByAdminId,
                                                              String idempotencyKey) {
        return mailDispatchJobRepository
                .findByRequestedByAdminIdAndIdempotencyKey(requestedByAdminId, idempotencyKey);
    }

    @Transactional
    public void recordUnknown(Long dispatchJobId, Long applyId) {
        // claim 경로와 같은 작업 우선 잠금으로 격리 결과와 집계를 원자적으로 처리한다.
        MailDispatchJob job = mailDispatchJobRepository.findByIdForUpdate(dispatchJobId)
                .orElseThrow(() -> new MailException(MailErrorCode.DISPATCH_JOB_NOT_FOUND));
        Optional<MailDispatchOutbox> outbox = mailDispatchOutboxRepository
                .findByDispatchJobIdAndApplyId(dispatchJobId, applyId);
        if (outbox.isPresent() && !outbox.get().quarantineUnclaimed()) {
            return;
        }
        // Outbox 도입 전 대상의 불확실 결과 저장도 유지한다.
        findTarget(dispatchJobId, applyId).markUnknown(MailErrorCode.MAIL_SEND_RESULT_UNKNOWN.getCode());
        job.recordUnknown();
    }

    @Transactional(readOnly = true)
    public MailDispatchResponse getResult(Long dispatchJobId) {
        return MailDispatchResponse.from(findJob(dispatchJobId));
    }

    private MailDispatchJob findJob(Long dispatchJobId) {
        return mailDispatchJobRepository.findById(dispatchJobId)
                .orElseThrow(() -> new MailException(MailErrorCode.DISPATCH_JOB_NOT_FOUND));
    }

    private MailDispatchTarget findTarget(Long dispatchJobId, Long applyId) {
        return mailDispatchTargetRepository.findByDispatchJobIdAndApplyId(dispatchJobId, applyId)
                .orElseThrow(() -> new MailException(MailErrorCode.INVALID_DISPATCH_TARGETS));
    }
}
