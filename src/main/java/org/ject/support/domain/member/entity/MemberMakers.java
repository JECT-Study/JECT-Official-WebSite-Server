package org.ject.support.domain.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.*;
import org.ject.support.domain.base.BaseTimeEntity;

@Entity
@Getter
@Builder
@Table(name = "member_makers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberMakers extends BaseTimeEntity {

    @Id
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", nullable = false)
    private MemberActivity memberActivity;

    @Column(length = 20)
    private String teamName;

    @Column(length = 30)
    private String mentoringAvailability;

    @Column(length = 30)
    private String projectSupplementAvailability;

    @Column(length = 30)
    private String speakerAvailability;

    @Column(length = 30)
    private String careerLevel;

    @Column(length = 255)
    private String skills;

    @Column(length = 30)
    private String company;

    @Column(length = 30)
    private String expertTopics;

    @Column(length = 20)
    private String activityCertNumber;
}
