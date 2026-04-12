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
 * 메일 발송 작업(Job)의 상태와 공통 변수 스냅샷을 관리하는 엔티티입니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "mail_dispatch_job")
public class MailDispatchJob extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scenario_id", nullable = false)
    private MailScenario scenario;

    @Column(name = "requested_by_member_id")
    private Long requestedByMemberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MailDispatchJobStatus status;

    @Column(nullable = false)
    private int receiverCount;

    @Column(name = "common_variables_json", columnDefinition = "TEXT")
    private String commonVariablesJson;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Builder
    public MailDispatchJob(MailScenario scenario,
                           Long requestedByMemberId,
                           MailDispatchJobStatus status,
                           int receiverCount,
                           String commonVariablesJson) {
        this.scenario = scenario;
        this.requestedByMemberId = requestedByMemberId;
        this.status = status;
        this.receiverCount = receiverCount;
        this.commonVariablesJson = commonVariablesJson;
    }

    /**
     * 발송 작업을 실행 중 상태로 전환하고 시작 시간을 기록합니다.
     */
    public void markProcessing() {
        this.status = MailDispatchJobStatus.PROCESSING;
        this.startedAt = LocalDateTime.now();
    }

    /**
     * 발송 작업 완료 상태로 전환하고 종료 시간을 기록합니다.
     */
    public void markCompleted() {
        this.status = MailDispatchJobStatus.COMPLETED;
        this.finishedAt = LocalDateTime.now();
    }

    /**
     * 발송 작업 실패 상태로 전환하고 종료 시간을 기록합니다.
     */
    public void markFailed() {
        this.status = MailDispatchJobStatus.FAILED;
        this.finishedAt = LocalDateTime.now();
    }
}
