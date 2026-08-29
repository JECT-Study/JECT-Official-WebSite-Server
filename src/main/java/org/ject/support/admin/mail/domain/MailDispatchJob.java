package org.ject.support.admin.mail.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
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
@Table(name = "mail_dispatch_job")
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

    @Column(name = "subject_template", nullable = false, columnDefinition = "TEXT")
    private String subjectTemplate;

    @Column(name = "body_template", nullable = false, columnDefinition = "TEXT")
    private String bodyTemplate;

    @Column(name = "input_variables_json", columnDefinition = "TEXT")
    private String inputVariablesJson;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Version
    private Long version;

    private MailDispatchJob(Long scenarioId,
                            Long recruitId,
                            Long requestedByAdminId,
                            String subjectTemplate,
                            String bodyTemplate,
                            String inputVariablesJson,
                            int targetCount) {
        this.scenarioId = scenarioId;
        this.recruitId = recruitId;
        this.requestedByAdminId = requestedByAdminId;
        this.subjectTemplate = subjectTemplate;
        this.bodyTemplate = bodyTemplate;
        this.inputVariablesJson = inputVariablesJson;
        this.targetCount = targetCount;
        this.status = MailDispatchJobStatus.REQUESTED;
    }

    public static MailDispatchJob create(Long scenarioId,
                                         Long recruitId,
                                         Long requestedByAdminId,
                                         String subjectTemplate,
                                         String bodyTemplate,
                                         String inputVariablesJson,
                                         int targetCount) {
        return new MailDispatchJob(
                scenarioId,
                recruitId,
                requestedByAdminId,
                subjectTemplate,
                bodyTemplate,
                inputVariablesJson,
                targetCount
        );
    }

    public void startProcessing() {
        validateStatus(MailDispatchJobStatus.REQUESTED);
        status = MailDispatchJobStatus.PROCESSING;
        processingCount = targetCount;
        startedAt = LocalDateTime.now();
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

    private void finishIfCompleted() {
        if (processingCount > 0) {
            return;
        }
        status = failedCount == targetCount
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
