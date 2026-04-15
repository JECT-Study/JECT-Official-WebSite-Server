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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ject.support.domain.base.BaseTimeEntity;

import java.time.LocalDateTime;

/**
 * 개별 수신 대상(Target)의 발송 결과를 저장하는 엔티티입니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "mail_dispatch_target")
public class MailDispatchTarget extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private MailDispatchJob job;

    @Column(name = "receiver_id")
    private Long receiverId;

    @Column(nullable = false, length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MailDispatchTargetStatus status;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Builder
    public MailDispatchTarget(MailDispatchJob job,
                              Long receiverId,
                              String email,
                              MailDispatchTargetStatus status,
                              String failureReason,
                              LocalDateTime sentAt) {
        this.job = job;
        this.receiverId = receiverId;
        this.email = email;
        this.status = status;
        this.failureReason = failureReason;
        this.sentAt = sentAt;
    }

    /**
     * 초기 대기 상태(PENDING)의 발송 대상을 생성합니다.
     */
    public static MailDispatchTarget pending(MailDispatchJob job, Long receiverId, String email) {
        return MailDispatchTarget.builder()
                .job(job)
                .receiverId(receiverId)
                .email(email)
                .status(MailDispatchTargetStatus.PENDING)
                .build();
    }

    /**
     * 발송 성공 상태로 전환합니다.
     */
    public void markSent() {
        this.status = MailDispatchTargetStatus.SENT;
        this.sentAt = LocalDateTime.now();
        this.failureReason = null;
    }

    /**
     * 발송 실패 상태로 전환하고 실패 사유를 기록합니다.
     */
    public void markFailed(String reason) {
        this.status = MailDispatchTargetStatus.FAILED;
        this.failureReason = reason;
        this.sentAt = null;
    }
}
