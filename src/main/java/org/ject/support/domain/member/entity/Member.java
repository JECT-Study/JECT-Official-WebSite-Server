package org.ject.support.domain.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.ject.support.common.util.StringListConverter;
import org.ject.support.domain.base.BaseTimeEntity;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Region;
import org.ject.support.domain.member.Role;

import java.util.ArrayList;
import java.util.List;

@SQLDelete(sql = "UPDATE member SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30, nullable = false, unique = true)
    private String email;

    @Column(length = 20)
    @Pattern(regexp = "^[가-힣]{1,5}$", message = "한글 1~5글자만 입력 가능합니다.")
    private String name;

    @Column(length = 12)
    @Pattern(regexp = "^010\\d{8}$", message = "010으로 시작하는 11자리 숫자를 입력하세요.")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(45)")
    private JobFamily jobFamily;

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

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(45)", nullable = false)
    @Builder.Default
    private MemberStatus status = MemberStatus.ACTIVE;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    @Builder.Default
    private List<TeamMember> teamMembers = new ArrayList<>();

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

    public MemberEditor.MemberEditorBuilder toEditor() {
        return MemberEditor.builder()
                .name(this.name)
                .phoneNumber(this.phoneNumber)
                .email(this.email)
                .semesterId(this.semesterId)
                .jobFamily(this.jobFamily)
                .region(this.region)
                .experiencePeriod(this.experiencePeriod)
                .careerDetails(this.careerDetails)
                .interestedDomains(this.interestedDomains)
                .role(this.role);
    }

    public void edit(MemberEditor editor) {
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
}
