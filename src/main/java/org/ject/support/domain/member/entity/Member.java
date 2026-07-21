package org.ject.support.domain.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
import org.ject.support.domain.member.Region;

import java.util.ArrayList;
import java.util.List;

@SQLDelete(sql = "UPDATE member SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30, nullable = false, unique = true)
    private String email;

    @Column(length = 20)
    @Size(max = 20)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Region region;

    @Column(length = 12)
    @Pattern(regexp = "^010\\d{8}$", message = "010으로 시작하는 11자리 숫자를 입력하세요.")
    private String phoneNumber;

    @Column(name = "domain_name", length = 50)
    @Convert(converter = StringListConverter.class)
    @Builder.Default
    private List<String> interestedDomains = new ArrayList<>();

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;


    public static Member create(
        String name,
        String email,
        String phoneNumber,
        List<String> interestedDomains,
        Region region
    ){
        return Member.builder()
            .name(name)
            .email(email)
            .phoneNumber(phoneNumber)
            .region(region)
            .interestedDomains(interestedDomains)
            .build();
    }

    // 삭제된 구성원 복구
    public void restore() {
        this.isDeleted = false;
    }

    // 기존 구성원 신상정보를 새 입력값으로 갱신
    public void updateProfile(
        String name,
        String phoneNumber,
        List<String> interestedDomains,
        Region region
    ) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.interestedDomains = interestedDomains;
        this.region = region;
    }
}
