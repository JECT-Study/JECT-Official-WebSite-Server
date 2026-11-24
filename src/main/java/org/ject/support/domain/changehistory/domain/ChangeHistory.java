package org.ject.support.domain.changehistory.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ject.support.domain.base.BaseTimeEntity;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChangeHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false)
    private String domain;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", length = 20, nullable = false)
    private ChangeActionType actionType;

    @Column(name = "target_id", length = 100)
    private String targetId;

    @Column(name = "actor_id")
    private Long actorId;

    @Builder.Default
    @OneToMany(mappedBy = "changeHistory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChangeDetail> changeDetails = new ArrayList<>();

    public static ChangeHistory create(
        String domain,
        ChangeActionType actionType,
        String targetId,
        Long actorId,
        List<ChangeDetail> changeDetails
    ) {
        ChangeHistory changeHistory = ChangeHistory.builder()
            .domain(domain)
            .actionType(actionType)
            .targetId(targetId)
            .actorId(actorId)
            .build();
        changeDetails.forEach(changeHistory::addChangeDetail);
        return changeHistory;
    }

    private void addChangeDetail(ChangeDetail changeDetail) {
        changeDetail.assignChangeHistory(this);
        this.changeDetails.add(changeDetail);
    }
}
