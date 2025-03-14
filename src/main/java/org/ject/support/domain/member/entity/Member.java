package org.ject.support.domain.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ject.support.domain.base.BaseTimeEntity;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Role;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20)
    @Pattern(regexp = "^[가-힣]{1,5}$", message = "한글 1~5글자만 입력 가능합니다.")
    private String name;

    @Column(length = 12)
    @Pattern(regexp = "^010\\d{8}$", message = "010으로 시작하는 11자리 숫자를 입력하세요.")
    private String phoneNumber;

    @Column(length = 30, nullable = false, unique = true)
    private String email;

    @Column(length = 20)
    private String semester;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(45)")
    private JobFamily jobFamily;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(10)", nullable = false)
    private Role role;

    @Column(length = 255)
    @NotNull
    private String pin;

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
}
