package org.ject.support.domain.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ject.support.domain.base.BaseTimeEntity;
import org.ject.support.domain.member.ParticipationStatus;

@Entity
@Getter
@Builder
@Table(name = "event_participation")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EventParticipation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_activity_id", nullable = false)
    private MemberActivity memberActivity;

    @Column(nullable = false, name = "semester_event_id")
    private Long semesterEventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "participation_status", nullable = false, length = 45)
    private ParticipationStatus participationStatus;

    // 행사 참여 기록 생성
    public static EventParticipation create(MemberActivity memberActivity, Long semesterEventId, ParticipationStatus participationStatus) {
        return EventParticipation.builder()
            .memberActivity(memberActivity)
            .semesterEventId(semesterEventId)
            .participationStatus(participationStatus)
            .build();
    }

    // 행사 참여 상태 변경
    public void updateStatus(ParticipationStatus participationStatus) {
        this.participationStatus = participationStatus;
    }
}
