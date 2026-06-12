package org.ject.support.domain.applicant.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    @Column(length = 30, nullable = false)
    private String email;

    @Column(length = 20)
    @Size(max = 20)
    private String name;

    @Column(length = 12)
    @Pattern(regexp = "^010\\d{8}$", message = "010으로 시작하는 11자리 숫자를 입력하세요.")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(45)")
    private JobFamily jobFamily;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_type", columnDefinition = "varchar(45)", nullable = false)
    @Builder.Default
    private MemberType memberType = MemberType.SEMESTER;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Region region;

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

    @Column(nullable = false)
    private Long semesterId;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(10)", nullable = false)
    private Role role;

    @Column(length = 255)
    private String pin;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(45)", nullable = false)
    @Builder.Default
    private MemberStatus status = MemberStatus.ACTIVE;

    public void updateNameAndPhoneNumber(String name, String phoneNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public void updatePin(String pin) {
        this.pin = pin;
    }

    public boolean isInitialed() {
        return this.name != null && this.phoneNumber != null;
    }

    public ApplicantEditor.ApplicantEditorBuilder toEditor() {
        return ApplicantEditor.builder()
                .name(this.name)
                .phoneNumber(this.phoneNumber)
                .email(this.email)
                .semesterId(this.semesterId)
                .jobFamily(this.jobFamily)
                .region(this.region)
                .experiencePeriod(this.experiencePeriod)
                .careerDetails(this.careerDetails)
                .interestedDomains(this.interestedDomains)
                .role(this.role)
                .status(this.status);
    }

    public void edit(ApplicantEditor editor) {
        this.name = editor.name();
        this.phoneNumber = editor.phoneNumber();
        this.email = editor.email();
        this.semesterId = editor.semesterId();
        this.jobFamily = editor.jobFamily();
        this.region = editor.region();
        this.experiencePeriod = editor.experiencePeriod();
        this.careerDetails = editor.careerDetails();
        this.interestedDomains = editor.interestedDomains();
        this.role = editor.role();
        this.status = editor.status();
    }

    public void deleteProfile() {
        this.name = null;
        this.phoneNumber = null;
        this.jobFamily = null;
        this.region = null;
        this.careerDetails = null;
        this.experiencePeriod = null;
        this.interestedDomains.clear();
    }

    public void promoteToSemester() {
        this.role = Role.SEMESTER;
    }

    public void updateMemberType(MemberType memberType) {
        this.memberType = Objects.requireNonNull(memberType, "memberType must not be null");
    }

    public boolean isProfileComplete() {
        return this.name != null && this.phoneNumber != null;
    }
}
