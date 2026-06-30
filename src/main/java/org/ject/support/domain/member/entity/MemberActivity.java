package org.ject.support.domain.member.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
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
import org.ject.support.domain.member.exception.MemberErrorCode;
import org.ject.support.domain.member.exception.MemberException;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
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

    @Column(name = "recruit_type_detail", length = 45, nullable = false)
    @Enumerated(EnumType.STRING)
    private RecruitTypeDetail recruitTypeDetail;

    @OneToOne(mappedBy = "memberActivity", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    private MemberSemester memberSemester;

    // 일반 구성원 활동과 관리 항목 생성
    public static MemberActivity createSemesterActivity(
        Long memberId,
        JobFamily jobFamily,
        RecruitTypeDetail recruitTypeDetail,
        CareerDetails careerDetails,
        ExperiencePeriod experiencePeriod,
        String memo,
        Long semesterId,
        Long teamId
    ){
        MemberActivity memberActivity = MemberActivity.builder()
            .memberId(memberId)
            .memberType(MemberType.SEMESTER)
            .jobFamily(jobFamily)
            .recruitTypeDetail(recruitTypeDetail)
            .careerDetails(careerDetails)
            .experiencePeriod(experiencePeriod)
            .memo(memo)
            .build();
        //애그리거트 루트인 MemberActivity쪽에서 식별 관계인 엔티티 생성 책임
        //MemberSemester는 MemberActivity없이 단독으로 사용되지 않음
        memberActivity.memberSemester = MemberSemester.create(memberActivity, semesterId, teamId);
        return memberActivity;
    }

    //Todo: 메이커스 구성원 활동과 관리 항목 생성

    //Todo: 운영 서포터즈 구성원 활동과 관리 항목 생성

    // 구성원 유형에 맞는 활동 상태로 변경
    public void updateActivityStatus(ActivityStatus activityStatus) {
        if (!activityStatus.isAvailableFor(memberType)) {
            throw new MemberException(MemberErrorCode.INVALID_ACTIVITY_STATUS);
        }

        this.activityStatus = activityStatus;
    }
}
