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
import org.hibernate.annotations.SQLDelete;
import org.ject.support.domain.base.BaseTimeEntity;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "member_semester")
@SQLDelete(sql = "UPDATE member_semester SET id = id WHERE id = ?")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberSemester extends BaseTimeEntity {

    @Id
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", nullable = false)
    private MemberActivity memberActivity;

    @Column(nullable = false)
    private Long semesterId;

    private Long teamId;

    @Column(length = 20)
    private String certNumber;

    @Column(length = 255)
    private String firstReview;

    @Column(length = 255)
    private String secondReview;

    public static MemberSemester create(
        MemberActivity memberActivity,
        Long semesterId,
        Long teamId
    ){
        return MemberSemester.builder()
            .memberActivity(memberActivity)
            .semesterId(semesterId)
            .teamId(teamId)
            .build();
    }

    // 일반 구성원 기수 변경
    public void changeSemester(Long semesterId) {
        this.semesterId = semesterId;
    }

    // 일반 구성원 팀 변경
    public void changeTeam(Long teamId) {
        this.teamId = teamId;
    }

    // 동일 기수 소속 여부 확인
    public boolean isSameSemester(Long semesterId) {
        return this.semesterId.equals(semesterId);
    }

    // 일반 구성원 활동정보 수정
    public void update(String certNumber, String firstReview, String secondReview) {
        if (certNumber != null) this.certNumber = certNumber;
        if (firstReview != null) this.firstReview = firstReview;
        if (secondReview != null) this.secondReview = secondReview;
    }

}
