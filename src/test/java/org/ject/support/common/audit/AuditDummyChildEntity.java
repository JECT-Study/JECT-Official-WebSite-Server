package org.ject.support.common.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 하나의 작업에서 여러 엔티티가 함께 변경되는 시나리오 검증
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class AuditDummyChildEntity implements Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String label;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private AuditDummyEntity parent;

    private AuditDummyChildEntity(String label) {
        this.label = label;
    }

    static AuditDummyChildEntity create(String label) {
        return new AuditDummyChildEntity(label);
    }

    void relabel(String label) {
        this.label = label;
    }

    void assignParent(AuditDummyEntity parent) {
        this.parent = parent;
    }
}
