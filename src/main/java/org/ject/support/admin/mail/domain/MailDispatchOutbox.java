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
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ject.support.admin.mail.exception.MailErrorCode;
import org.ject.support.admin.mail.exception.MailException;
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

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MailDispatchOutboxStatus status;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "claim_token", length = 36)
    private String claimToken;

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

    public static MailDispatchOutbox createPending(MailDispatchJob dispatchJob,
                                                   Long applyId,
                                                   String email,
                                                   String subject,
                                                   String body) {
        return new MailDispatchOutbox(dispatchJob, applyId, email, subject, body);
    }

    public void markSent() {
        validatePending();
        status = MailDispatchOutboxStatus.SENT;
        failureReason = null;
    }

    public void markFailed(String failureReason) {
        validatePending();
        status = MailDispatchOutboxStatus.FAILED;
        this.failureReason = failureReason;
    }

    public boolean claim(String claimToken, LocalDateTime now, LocalDateTime leaseUntil) {
        Objects.requireNonNull(claimToken);
        Objects.requireNonNull(now);
        Objects.requireNonNull(leaseUntil);
        if (!leaseUntil.isAfter(now)) {
            throw new IllegalArgumentException("leaseUntil must be after now");
        }
        if (status != MailDispatchOutboxStatus.PENDING) {
            return false;
        }
        this.claimToken = claimToken;
        this.leaseUntil = leaseUntil;
        status = MailDispatchOutboxStatus.PROCESSING;
        return true;
    }

    public boolean quarantineExpired(LocalDateTime now) {
        Objects.requireNonNull(now);
        if (status != MailDispatchOutboxStatus.PROCESSING
                || leaseUntil == null || leaseUntil.isAfter(now)) {
            return false;
        }
        // 만료된 실행은 SES 수락 여부를 알 수 없어 자동 재발송하지 않는다.
        status = MailDispatchOutboxStatus.UNKNOWN;
        failureReason = MailErrorCode.MAIL_SEND_RESULT_UNKNOWN.getCode();
        return true;
    }

    public boolean quarantineUnclaimed() {
        if (status != MailDispatchOutboxStatus.PENDING) {
            return false;
        }
        status = MailDispatchOutboxStatus.UNKNOWN;
        failureReason = MailErrorCode.MAIL_SEND_RESULT_UNKNOWN.getCode();
        return true;
    }

    public boolean hasClaimToken(String token) {
        return status == MailDispatchOutboxStatus.PROCESSING
                && token != null && token.equals(claimToken);
    }

    public boolean markClaimSent(String token, LocalDateTime now) {
        if (!hasActiveClaim(token, now)) {
            return false;
        }
        status = MailDispatchOutboxStatus.SENT;
        failureReason = null;
        return true;
    }

    public boolean markClaimFailed(String token, LocalDateTime now, String failureReason) {
        if (!hasActiveClaim(token, now)) {
            return false;
        }
        status = MailDispatchOutboxStatus.FAILED;
        this.failureReason = failureReason;
        return true;
    }

    private boolean hasActiveClaim(String token, LocalDateTime now) {
        Objects.requireNonNull(now);
        return hasClaimToken(token) && leaseUntil != null && leaseUntil.isAfter(now);
    }

    private void validatePending() {
        if (status != MailDispatchOutboxStatus.PENDING) {
            throw new MailException(MailErrorCode.INVALID_DISPATCH_TARGET_STATUS);
        }
    }
}
