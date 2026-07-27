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
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "member_supporters")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberSupporters extends BaseTimeEntity {

    @Id
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", nullable = false)
    private MemberActivity memberActivity;

    @Column(length = 20)
    private String activityCertNumber;

    // 운영 서포터즈 관리 항목 생성
    public static MemberSupporters create(MemberActivity memberActivity, String activityCertNumber) {
        return MemberSupporters.builder()
            .memberActivity(memberActivity)
            .activityCertNumber(activityCertNumber)
            .build();
    }
}
