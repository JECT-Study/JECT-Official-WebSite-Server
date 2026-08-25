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

}
