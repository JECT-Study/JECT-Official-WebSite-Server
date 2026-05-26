package org.ject.support.domain.member.entity;

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
import java.time.LocalDate;

import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.ject.support.domain.base.BaseTimeEntity;
import org.ject.support.domain.member.ActivityStatus;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MemberType;

@Entity
@Getter
@Builder
@Table(name = "member_activity")
@SQLDelete(sql = "UPDATE member_activity SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberActivity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private CareerDetails careerDetails;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_type", columnDefinition = "varchar(45)", nullable = false)
    private MemberType memberType;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(45)")
    private JobFamily jobFamily;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(45)", nullable = false)
    @Builder.Default
    private ActivityStatus activityStatus = ActivityStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ExperiencePeriod experiencePeriod;

    private LocalDate startDate;

    private LocalDate endDate;

    @Column(length = 100)
    private String memo;
}
