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
@Table(name = "mail_dispatch_target")
public class MailDispatchTarget extends BaseTimeEntity {

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MailDispatchTargetStatus status;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Version
    private Long version;

    private MailDispatchTarget(MailDispatchJob dispatchJob, Long applyId, String email) {
        this.dispatchJob = dispatchJob;
        this.applyId = applyId;
        this.email = email;
        this.status = MailDispatchTargetStatus.PENDING;
    }

    public static MailDispatchTarget pending(MailDispatchJob dispatchJob, Long applyId, String email) {
        return new MailDispatchTarget(dispatchJob, applyId, email);
    }

    public void markSent() {
        validatePending();
        status = MailDispatchTargetStatus.SENT;
        sentAt = LocalDateTime.now();
        failureReason = null;
    }

    public void markFailed(String failureReason) {
        validatePending();
        status = MailDispatchTargetStatus.FAILED;
        this.failureReason = failureReason;
        sentAt = null;
    }

    private void validatePending() {
        if (status != MailDispatchTargetStatus.PENDING) {
            throw new MailException(MailErrorCode.INVALID_DISPATCH_TARGET_STATUS);
        }
    }
}
