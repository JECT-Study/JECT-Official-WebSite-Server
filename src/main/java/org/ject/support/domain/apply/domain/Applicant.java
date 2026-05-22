package org.ject.support.domain.apply.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.ject.support.common.util.StringListConverter;
import org.ject.support.domain.base.BaseTimeEntity;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.MemberType;
import org.ject.support.domain.member.Region;
import org.ject.support.domain.member.Role;

@Entity
@Getter
@Builder
@Table(name = "applicant")
@SQLDelete(sql = "UPDATE applicant SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Applicant extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20)
    private String name;

    @Column(length = 12)
    private String phoneNumber;

    @Column(length = 30, nullable = false)
    private String email;

    @Column(nullable = false)
    private Long semesterId;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(45)")
    private JobFamily jobFamily;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(10)", nullable = false)
    private Role role;

    @Column(length = 255)
    private String pin;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(45)", nullable = false)
    @Builder.Default
    private MemberStatus status = MemberStatus.ACTIVE;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private CareerDetails careerDetails;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ExperiencePeriod experiencePeriod;

    @Column(name = "domain_name", length = 50)
    @Convert(converter = StringListConverter.class)
    @Builder.Default
    private List<String> interestedDomains = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Region region;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_type", columnDefinition = "varchar(45)", nullable = false)
    @Builder.Default
    private MemberType memberType = MemberType.SEMESTER;
}
