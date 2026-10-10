package org.ject.support.admin.mail.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ject.support.admin.mail.exception.MailErrorCode;
import org.ject.support.admin.mail.exception.MailException;
import org.ject.support.domain.base.BaseTimeEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "mail_dispatch_job", uniqueConstraints = @UniqueConstraint(
        name = "uk_mail_dispatch_job_requester_key",
        columnNames = {"requested_by_admin_id", "idempotency_key"}))
public class MailDispatchJob extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scenario_id", nullable = false)
    private Long scenarioId;

    @Column(name = "recruit_id", nullable = false)
    private Long recruitId;

    @Column(name = "requested_by_admin_id", nullable = false)
    private Long requestedByAdminId;

    @Column(name = "idempotency_key", nullable = false, length = 255)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MailDispatchJobStatus status;

    @Column(name = "target_count", nullable = false)
    private int targetCount;

    @Column(name = "processing_count", nullable = false)
    private int processingCount;

    @Column(name = "success_count", nullable = false)
    private int successCount;

    @Column(name = "failed_count", nullable = false)
    private int failedCount;

    @Column(name = "unknown_count", nullable = false)
    private int unknownCount;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "subject_template", nullable = false, columnDefinition = "TEXT")
    private String subjectTemplate;

    @Column(name = "body_template", nullable = false, columnDefinition = "TEXT")
    private String bodyTemplate;

    @Column(name = "input_variables_json", columnDefinition = "TEXT")
    private String inputVariablesJson;

    @Column(name = "request_fingerprint", columnDefinition = "TEXT")
    private String requestFingerprint;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "claim_started_at")
    private LocalDateTime claimStartedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "scheduled_at", columnDefinition = "DATETIME(6)")
    private Instant scheduledAt;

    @Version
    private Long version;

    private MailDispatchJob(Long scenarioId,
                            Long recruitId,
                            Long requestedByAdminId,
                            String idempotencyKey,
                            String subjectTemplate,
                            String bodyTemplate,
                            String inputVariablesJson,
                            String requestFingerprint,
                            int targetCount) {
        this.scenarioId = scenarioId;
        this.recruitId = recruitId;
        this.requestedByAdminId = requestedByAdminId;
        this.idempotencyKey = idempotencyKey;
        this.subjectTemplate = subjectTemplate;
        this.bodyTemplate = bodyTemplate;
        this.inputVariablesJson = inputVariablesJson;
        this.requestFingerprint = requestFingerprint;
        this.targetCount = targetCount;
        this.status = MailDispatchJobStatus.REQUESTED;
        this.requestedAt = LocalDateTime.now();
    }

    public static MailDispatchJob create(Long scenarioId,
                                         Long recruitId,
                                         Long requestedByAdminId,
                                         String idempotencyKey,
                                         String subjectTemplate,
                                         String bodyTemplate,
                                         String inputVariablesJson,
                                         int targetCount) {
        return create(
                scenarioId,
                recruitId,
                requestedByAdminId,
                idempotencyKey,
                subjectTemplate,
                bodyTemplate,
                inputVariablesJson,
                null,
                targetCount
        );
    }

    public static MailDispatchJob create(Long scenarioId,
                                         Long recruitId,
                                         Long requestedByAdminId,
                                         String idempotencyKey,
                                         String subjectTemplate,
                                         String bodyTemplate,
                                         String inputVariablesJson,
                                         String requestFingerprint,
                                         int targetCount) {
        if (targetCount <= 0) {
            throw new MailException(MailErrorCode.INVALID_DISPATCH_TARGET_COUNT);
        }
        return new MailDispatchJob(
                scenarioId,
                recruitId,
                requestedByAdminId,
                idempotencyKey,
                subjectTemplate,
                bodyTemplate,
                inputVariablesJson,
                requestFingerprint,
                targetCount
        );
    }

    public void schedule(Instant scheduledAt) {
        validateStatus(MailDispatchJobStatus.REQUESTED);
        if (scheduledAt == null || !scheduledAt.isAfter(Instant.now())) {
            throw new MailException(MailErrorCode.INVALID_SCHEDULED_AT);
        }
        this.scheduledAt = scheduledAt;
        status = MailDispatchJobStatus.SCHEDULED;
    }

    public void startProcessing() {
        validateStatus(MailDispatchJobStatus.REQUESTED);
        status = MailDispatchJobStatus.PROCESSING;
        processingCount = targetCount;
        startedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (status != MailDispatchJobStatus.SCHEDULED) {
            throw new MailException(MailErrorCode.DISPATCH_CANCELLATION_NOT_ALLOWED);
        }
        status = MailDispatchJobStatus.CANCELLED;
        finishedAt = LocalDateTime.now();
    }

    public void startClaimProcessing() {
        startProcessing();
        claimStartedAt = startedAt;
    }

    public void recordSuccess() {
        validateStatus(MailDispatchJobStatus.PROCESSING);
        processingCount--;
        successCount++;
        finishIfCompleted();
    }

    public void recordFailure() {
        validateStatus(MailDispatchJobStatus.PROCESSING);
        processingCount--;
        failedCount++;
        finishIfCompleted();
    }

    public void recordUnknown() {
        validateStatus(MailDispatchJobStatus.PROCESSING);
        processingCount--;
        unknownCount++;
        finishIfCompleted();
    }

    private void finishIfCompleted() {
        if (processingCount > 0) {
            return;
        }
        // 모든 대상이 실패한 경우에만 작업을 실패로 마무리한다.
        status = unknownCount > 0
                ? MailDispatchJobStatus.UNKNOWN
                : failedCount == targetCount
                ? MailDispatchJobStatus.FAILED
                : MailDispatchJobStatus.COMPLETED;
        finishedAt = LocalDateTime.now();
    }

    private void validateStatus(MailDispatchJobStatus expectedStatus) {
        if (status != expectedStatus) {
            throw new MailException(MailErrorCode.INVALID_DISPATCH_JOB_STATUS);
        }
    }
}
