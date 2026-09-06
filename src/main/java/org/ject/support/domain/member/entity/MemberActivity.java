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
import org.ject.support.domain.member.Availability;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.CareerLevel;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MakersTeam;
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

    @OneToOne(mappedBy = "memberActivity", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    private MemberMakers memberMakers;

    @OneToOne(mappedBy = "memberActivity", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    private MemberSupporters memberSupporters;

    // 일반 구성원 활동과 관리 항목 생성
    public static MemberActivity createSemesterActivity(
        Long memberId,
        JobFamily jobFamily,
        RecruitTypeDetail recruitTypeDetail,
        ActivityStatus activityStatus,
        CareerDetails careerDetails,
        ExperiencePeriod experiencePeriod,
        String memo,
        Long semesterId,
        Long teamId
    ){
        validateJobFamily(MemberType.SEMESTER, jobFamily);
        validateActivityStatus(MemberType.SEMESTER, activityStatus);

        MemberActivity memberActivity = MemberActivity.builder()
            .memberId(memberId)
            .memberType(MemberType.SEMESTER)
            .jobFamily(jobFamily)
            .recruitTypeDetail(recruitTypeDetail)
            .activityStatus(activityStatus)
            .careerDetails(careerDetails)
            .experiencePeriod(experiencePeriod)
            .memo(memo)
            .build();
        //애그리거트 루트인 MemberActivity쪽에서 식별 관계인 엔티티 생성 책임
        //MemberSemester는 MemberActivity없이 단독으로 사용되지 않음
        memberActivity.memberSemester = MemberSemester.create(memberActivity, semesterId, teamId);
        return memberActivity;
    }

    // 메이커스팀 구성원 활동과 관리 항목 생성
    public static MemberActivity createMakersActivity(
        Long memberId,
        JobFamily jobFamily,
        RecruitTypeDetail recruitTypeDetail,
        ActivityStatus activityStatus,
        CareerDetails careerDetails,
        ExperiencePeriod experiencePeriod,
        String memo,
        MakersTeam makersTeam,
        Availability mentoringAvailability,
        Availability projectSupplementAvailability,
        Availability speakerAvailability,
        CareerLevel careerLevel,
        String skills,
        String company,
        String expertTopics,
        String activityCertNumber
    ){
        validateJobFamily(MemberType.MAKERS, jobFamily);
        validateActivityStatus(MemberType.MAKERS, activityStatus);

        MemberActivity memberActivity = MemberActivity.builder()
            .memberId(memberId)
            .memberType(MemberType.MAKERS)
            .jobFamily(jobFamily)
            .recruitTypeDetail(recruitTypeDetail)
            .activityStatus(activityStatus)
            .careerDetails(careerDetails)
            .experiencePeriod(experiencePeriod)
            .memo(memo)
            .build();
        // 애그리거트 루트인 MemberActivity쪽에서 식별 관계인 엔티티 생성 책임
        memberActivity.memberMakers = MemberMakers.create(
            memberActivity,
            makersTeam,
            mentoringAvailability,
            projectSupplementAvailability,
            speakerAvailability,
            careerLevel,
            skills,
            company,
            expertTopics,
            activityCertNumber
        );
        return memberActivity;
    }

    // 운영 서포터즈 구성원 활동과 관리 항목 생성
    public static MemberActivity createSupportersActivity(
        Long memberId,
        JobFamily jobFamily,
        RecruitTypeDetail recruitTypeDetail,
        ActivityStatus activityStatus,
        LocalDate startDate,
        LocalDate endDate,
        String activityCertNumber,
        String memo
    ){
        validateJobFamily(MemberType.SUPPORTERS, jobFamily);
        validateActivityStatus(MemberType.SUPPORTERS, activityStatus);
        validateActivityPeriod(startDate, endDate);

        MemberActivity memberActivity = MemberActivity.builder()
            .memberId(memberId)
            .memberType(MemberType.SUPPORTERS)
            .jobFamily(jobFamily)
            .recruitTypeDetail(recruitTypeDetail)
            .activityStatus(activityStatus)
            .startDate(startDate)
            .endDate(endDate)
            .memo(memo)
            .build();
        // 애그리거트 루트인 MemberActivity쪽에서 식별 관계인 엔티티 생성 책임
        memberActivity.memberSupporters = MemberSupporters.create(memberActivity, activityCertNumber);
        return memberActivity;
    }

    // 구성원 유형에 맞는 활동 상태로 변경
    public void updateActivityStatus(ActivityStatus activityStatus) {
        changeActivityStatus(activityStatus);
    }

    // 전달된 구성원 활동정보 편집
    public void edit(JobFamily jobFamily, CareerDetails careerDetails, RecruitTypeDetail recruitTypeDetail,
        ExperiencePeriod experiencePeriod, String memo) {
        if (jobFamily != null) {
            validateJobFamily(memberType, jobFamily);
            this.jobFamily = jobFamily;
        }
        if (careerDetails != null) this.careerDetails = careerDetails;
        if (recruitTypeDetail != null) this.recruitTypeDetail = recruitTypeDetail;
        if (experiencePeriod != null) this.experiencePeriod = experiencePeriod;
        if (memo != null) this.memo = memo;
    }

    // 메이커스팀 구성원 활동정보 편집
    public void editMakersActivity(JobFamily jobFamily, CareerDetails careerDetails, RecruitTypeDetail recruitTypeDetail,
        ExperiencePeriod experiencePeriod, String memo, MakersTeam makersTeam, Availability mentoringAvailability,
        Availability projectSupplementAvailability, Availability speakerAvailability, CareerLevel careerLevel,
        String skills, String company, String expertTopics, String activityCertNumber) {
        edit(jobFamily, careerDetails, recruitTypeDetail, experiencePeriod, memo);
        memberMakers.edit(makersTeam, mentoringAvailability, projectSupplementAvailability, speakerAvailability,
            careerLevel, skills, company, expertTopics, activityCertNumber);
    }

    public void activate() {
        changeActivityStatus(ActivityStatus.ACTIVE);
    }

    public void end() {
        changeActivityStatus(ActivityStatus.ENDED);
    }

    public void dropOut() {
        changeActivityStatus(ActivityStatus.DROPOUT);
    }

    public boolean isSameActivityStatus(ActivityStatus activityStatus) {
        return this.activityStatus == activityStatus;
    }

    private void changeActivityStatus(ActivityStatus nextStatus) {
        validateActivityStatus(memberType, nextStatus);
        this.activityStatus = nextStatus;
    }

    // 구성원 활동 삭제 처리
    public void delete(){
        this.isDeleted = true;
    }

    // 구성원 유형별 활동 상태 검증
    private static void validateActivityStatus(MemberType memberType, ActivityStatus activityStatus) {
        if (activityStatus == null || !activityStatus.isAvailableFor(memberType)) {
            throw new MemberException(MemberErrorCode.INVALID_ACTIVITY_STATUS);
        }
    }

    // 구성원 유형별 포지션 검증
    private static void validateJobFamily(MemberType memberType, JobFamily jobFamily) {
        if (jobFamily == null || !jobFamily.isAvailableFor(memberType)) {
            throw new MemberException(MemberErrorCode.INVALID_JOB_FAMILY);
        }
    }

    // 활동 시작일과 종료일 순서 검증
    private static void validateActivityPeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate != null) {
            throw new MemberException(MemberErrorCode.INVALID_ACTIVITY_PERIOD);
        }
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new MemberException(MemberErrorCode.INVALID_ACTIVITY_PERIOD);
        }
    }
}
