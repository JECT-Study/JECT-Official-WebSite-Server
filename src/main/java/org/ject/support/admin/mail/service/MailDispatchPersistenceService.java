package org.ject.support.admin.mail.service;

import java.util.List;
import java.util.Optional;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.mail.domain.MailDispatchJob;
import org.ject.support.admin.mail.domain.MailDispatchOutbox;
import org.ject.support.admin.mail.domain.MailDispatchOutboxStatus;
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

    private static final Duration OUTBOX_LEASE_DURATION = Duration.ofMinutes(1);

    private final MailDispatchJobRepository mailDispatchJobRepository;
    private final MailDispatchTargetRepository mailDispatchTargetRepository;
    private final MailDispatchOutboxRepository mailDispatchOutboxRepository;
    private final Map2JsonSerializer map2JsonSerializer;

    @Transactional
    public MailDispatchJob createJob(MailDispatchPlan plan, String requestFingerprint) {
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
        MailDispatchJob savedJob = mailDispatchJobRepository.save(job);
        List<MailDispatchTarget> targets = plan.targets().stream()
                .map(target -> MailDispatchTarget.pending(savedJob, target.applyId(), target.email()))
                .toList();
        mailDispatchTargetRepository.saveAll(targets);
        List<MailDispatchOutbox> outboxes = plan.targets().stream()
                .map(target -> MailDispatchOutbox.pending(
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
        findOutbox(dispatchJobId, applyId).ifPresent(MailDispatchOutbox::markSent);
    }

    @Transactional
    public void recordFailure(Long dispatchJobId, Long applyId, String failureReason) {
        MailDispatchJob job = findJob(dispatchJobId);
        MailDispatchTarget target = findTarget(dispatchJobId, applyId);
        target.markFailed(failureReason);
        job.recordFailure();
        findOutbox(dispatchJobId, applyId).ifPresent(outbox -> outbox.markFailed(failureReason));
    }

    @Transactional
    public Optional<MailDispatchOutbox> claimForImmediateDispatch(Long dispatchJobId,
                                                                    Long applyId,
                                                                    String claimedBy) {
        return mailDispatchOutboxRepository.findByDispatchJobIdAndApplyId(dispatchJobId, applyId)
                .flatMap(outbox -> claimOutbox(outbox.getId(), claimedBy));
    }

    @Transactional
    public Optional<MailDispatchOutbox> claimOutbox(Long outboxId, String claimedBy) {
        LocalDateTime now = LocalDateTime.now();
        int claimed = mailDispatchOutboxRepository.claim(
                outboxId,
                claimedBy,
                now,
                now.plus(OUTBOX_LEASE_DURATION),
                MailDispatchOutboxStatus.PENDING,
                MailDispatchOutboxStatus.PROCESSING);
        if (claimed == 0) {
            return Optional.empty();
        }
        return mailDispatchOutboxRepository.findById(outboxId);
    }

    @Transactional(readOnly = true)
    public Optional<MailDispatchResponse> findResultByIdempotencyKey(Long requestedByAdminId,
                                                                      String idempotencyKey) {
        return mailDispatchJobRepository
                .findByRequestedByAdminIdAndIdempotencyKey(requestedByAdminId, idempotencyKey)
                .map(MailDispatchResponse::from);
    }

    @Transactional(readOnly = true)
    public Optional<String> findRequestFingerprintByIdempotencyKey(Long requestedByAdminId,
                                                                    String idempotencyKey) {
        return mailDispatchJobRepository
                .findByRequestedByAdminIdAndIdempotencyKey(requestedByAdminId, idempotencyKey)
                .map(MailDispatchJob::getRequestFingerprint);
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

    private Optional<MailDispatchOutbox> findOutbox(Long dispatchJobId, Long applyId) {
        return mailDispatchOutboxRepository.findByDispatchJobIdAndApplyId(dispatchJobId, applyId);
    }
}
