package org.ject.support.admin.mail.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ject.support.domain.base.BaseTimeEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "mail_dispatch_outbox", uniqueConstraints = @UniqueConstraint(
        name = "uk_mail_dispatch_outbox_job_apply",
        columnNames = {"dispatch_job_id", "apply_id"}))
public class MailDispatchOutbox extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dispatch_job_id", nullable = false)
    private MailDispatchJob dispatchJob;

    @Column(name = "apply_id", nullable = false)
    private Long applyId;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MailDispatchOutboxStatus status;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "claimed_by", length = 100)
    private String claimedBy;

    @Column(name = "lease_until")
    private LocalDateTime leaseUntil;

    @Version
    private Long version;

    private MailDispatchOutbox(MailDispatchJob dispatchJob,
                               Long applyId,
                               String email,
                               String subject,
                               String body) {
        this.dispatchJob = dispatchJob;
        this.applyId = applyId;
        this.email = email;
        this.subject = subject;
        this.body = body;
        this.status = MailDispatchOutboxStatus.PENDING;
    }

    public static MailDispatchOutbox pending(MailDispatchJob dispatchJob,
                                             Long applyId,
                                             String email,
                                             String subject,
                                             String body) {
        return new MailDispatchOutbox(dispatchJob, applyId, email, subject, body);
    }

    public void markSent() {
        status = MailDispatchOutboxStatus.SENT;
        failureReason = null;
    }

    public void markFailed(String failureReason) {
        status = MailDispatchOutboxStatus.FAILED;
        this.failureReason = failureReason;
    }
}
