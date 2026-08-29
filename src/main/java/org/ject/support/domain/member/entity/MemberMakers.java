package org.ject.support.domain.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.ject.support.domain.base.BaseTimeEntity;
import org.ject.support.domain.member.Availability;
import org.ject.support.domain.member.MakersTeam;
import org.ject.support.domain.member.CareerLevel;
import org.ject.support.domain.member.command.EditMemberMakersCommand;

@Entity
@Getter
@Builder
@Table(name = "member_makers")
@SQLDelete(sql = "UPDATE member_makers SET id = id WHERE id = ?")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberMakers extends BaseTimeEntity {

    @Id
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", nullable = false)
    private MemberActivity memberActivity;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private MakersTeam makersTeam;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Availability mentoringAvailability;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Availability projectSupplementAvailability;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Availability speakerAvailability;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private CareerLevel careerLevel;

    @Column(length = 255)
    private String skills;

    @Column(length = 30)
    private String company;

    @Column(length = 30)
    private String expertTopics;

    @Column(length = 20)
    private String activityCertNumber;

    public static MemberMakers create(
        MemberActivity memberActivity,
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
        return MemberMakers.builder()
            .memberActivity(memberActivity)
            .makersTeam(makersTeam)
            .mentoringAvailability(mentoringAvailability)
            .projectSupplementAvailability(projectSupplementAvailability)
            .speakerAvailability(speakerAvailability)
            .careerLevel(careerLevel)
            .skills(skills)
            .company(company)
            .expertTopics(expertTopics)
            .activityCertNumber(activityCertNumber)
            .build();
    }

    // 전달된 메이커스팀 상세정보 편집
    public void edit(EditMemberMakersCommand command) {
        if (command.makersTeam() != null) this.makersTeam = command.makersTeam();
        if (command.mentoringAvailability() != null) this.mentoringAvailability = command.mentoringAvailability();
        if (command.projectSupplementAvailability() != null) this.projectSupplementAvailability = command.projectSupplementAvailability();
        if (command.speakerAvailability() != null) this.speakerAvailability = command.speakerAvailability();
        if (command.careerLevel() != null) this.careerLevel = command.careerLevel();
        if (command.skills() != null) this.skills = command.skills();
        if (command.company() != null) this.company = command.company();
        if (command.expertTopics() != null) this.expertTopics = command.expertTopics();
        if (command.activityCertNumber() != null) this.activityCertNumber = command.activityCertNumber();
    }
}
