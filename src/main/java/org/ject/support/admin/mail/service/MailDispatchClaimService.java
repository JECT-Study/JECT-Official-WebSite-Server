package org.ject.support.admin.mail.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.mail.domain.MailDispatchJob;
import org.ject.support.admin.mail.domain.MailDispatchJobStatus;
import org.ject.support.admin.mail.domain.MailDispatchOutbox;
import org.ject.support.admin.mail.domain.MailDispatchTarget;
import org.ject.support.admin.mail.dto.MailDispatchOutboxClaim;
import org.ject.support.admin.mail.exception.MailErrorCode;
import org.ject.support.admin.mail.exception.MailException;
import org.ject.support.admin.mail.repository.MailDispatchJobRepository;
import org.ject.support.admin.mail.repository.MailDispatchOutboxRepository;
import org.ject.support.admin.mail.repository.MailDispatchTargetRepository;
import org.ject.support.common.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class MailDispatchClaimService {

    private final MailDispatchOutboxRepository mailDispatchOutboxRepository;
    private final MailDispatchJobRepository mailDispatchJobRepository;
    private final MailDispatchTargetRepository mailDispatchTargetRepository;

    // 호출자의 트랜잭션과 분리해 외부 발송 전에 claim 커밋을 완료한다.
    public Optional<MailDispatchOutboxClaim> claim(Long outboxId, LocalDateTime now, Duration leaseDuration) {
        Objects.requireNonNull(now);
        Objects.requireNonNull(leaseDuration);
        if (leaseDuration.isZero() || leaseDuration.isNegative()) {
            throw new IllegalArgumentException("leaseDuration must be positive");
        }
        return findLockedExecution(outboxId)
                .filter(execution -> claimIfSafe(execution, now, leaseDuration))
                .map(execution -> {
                    startIfRequested(execution.job());
                    return MailDispatchOutboxClaim.from(execution.outbox());
                });
    }

    public boolean quarantineExpired(Long outboxId, LocalDateTime now) {
        return findLockedExecution(outboxId)
                .map(execution -> quarantineIfExpired(execution, now))
                .orElse(false);
    }

    public boolean recordSuccess(Long outboxId, String claimToken, LocalDateTime now) {
        return recordResult(outboxId, claimToken, now, null);
    }

    public boolean recordFailure(Long outboxId, String claimToken, LocalDateTime now, ErrorCode errorCode) {
        return recordResult(outboxId, claimToken, now, Objects.requireNonNull(errorCode).getCode());
    }

    private boolean recordResult(Long outboxId, String claimToken, LocalDateTime now, String failureCode) {
        return findLockedExecution(outboxId).map(execution -> {
            if (!execution.outbox().hasClaimToken(claimToken)) {
                return false;
            }
            if (quarantineIfExpired(execution, now)) {
                return false;
            }
            boolean recorded = failureCode == null
                    ? execution.outbox().markClaimSent(claimToken, now)
                    : execution.outbox().markClaimFailed(claimToken, now, failureCode);
            if (!recorded) {
                return false;
            }
            startIfRequested(execution.job());
            if (failureCode == null) {
                execution.target().markSent();
                execution.job().recordSuccess();
            } else {
                execution.target().markFailed(failureCode);
                execution.job().recordFailure();
            }
            return true;
        }).orElse(false);
    }

    private boolean quarantineIfExpired(LockedExecution execution, LocalDateTime now) {
        if (!execution.outbox().quarantineExpired(now)) {
            return false;
        }
        recordUnknown(execution);
        return true;
    }

    private boolean claimIfSafe(LockedExecution execution, LocalDateTime now, Duration leaseDuration) {
        MailDispatchJob job = execution.job();
        if (job.getStatus() == MailDispatchJobStatus.PROCESSING && job.getClaimStartedAt() == null) {
            // 기존 동기 실행의 PENDING은 SES 미호출 증거가 아니므로 재발송하지 않는다.
            if (execution.outbox().quarantineUnclaimed()) {
                recordUnknown(execution);
            }
            return false;
        }
        if (job.getStatus() != MailDispatchJobStatus.REQUESTED
                && job.getStatus() != MailDispatchJobStatus.PROCESSING) {
            return false;
        }
        return execution.outbox().claim(UUID.randomUUID().toString(), now, now.plus(leaseDuration));
    }

    private void recordUnknown(LockedExecution execution) {
        startIfRequested(execution.job());
        execution.target().markUnknown(MailErrorCode.MAIL_SEND_RESULT_UNKNOWN.getCode());
        execution.job().recordUnknown();
    }

    private void startIfRequested(MailDispatchJob job) {
        if (job.getStatus() == MailDispatchJobStatus.REQUESTED) {
            job.startClaimProcessing();
        }
    }

    private Optional<LockedExecution> findLockedExecution(Long outboxId) {
        // 대상별 결과 집계를 직렬화하고 작업·Outbox 순서로 잠가 교착을 방지한다.
        return mailDispatchOutboxRepository.findDispatchJobIdById(outboxId).map(jobId -> {
            MailDispatchJob job = mailDispatchJobRepository.findByIdForUpdate(jobId)
                    .orElseThrow(() -> new MailException(MailErrorCode.DISPATCH_JOB_NOT_FOUND));
            MailDispatchOutbox outbox = mailDispatchOutboxRepository.findByIdForUpdate(outboxId)
                    .orElseThrow(() -> new MailException(MailErrorCode.INVALID_DISPATCH_TARGETS));
            MailDispatchTarget target = mailDispatchTargetRepository
                    .findByDispatchJobIdAndApplyId(jobId, outbox.getApplyId())
                    .orElseThrow(() -> new MailException(MailErrorCode.INVALID_DISPATCH_TARGETS));
            return new LockedExecution(job, outbox, target);
        });
    }

    private record LockedExecution(MailDispatchJob job, MailDispatchOutbox outbox, MailDispatchTarget target) {
    }
}
