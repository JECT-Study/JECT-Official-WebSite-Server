package org.ject.support.domain.changehistory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChangeDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "change_history_id", nullable = false)
    private ChangeHistory changeHistory;

    @Column(name = "entity_name", length = 100, nullable = false)
    private String entityName;

    @Column(name = "entity_id", length = 100)
    private String entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", length = 20, nullable = false)
    private ChangeActionType actionType;

    @Column(name = "before_data", columnDefinition = "TEXT")
    private String beforeData;

    @Column(name = "after_data", columnDefinition = "TEXT")
    private String afterData;

    public static ChangeDetail create(
        String entityName,
        String entityId,
        ChangeActionType actionType,
        String beforeData,
        String afterData
    ) {
        return ChangeDetail.builder()
            .entityName(entityName)
            .entityId(entityId)
            .actionType(actionType)
            .beforeData(beforeData)
            .afterData(afterData)
            .build();
    }

    void assignChangeHistory(ChangeHistory changeHistory) {
        this.changeHistory = changeHistory;
    }
}
