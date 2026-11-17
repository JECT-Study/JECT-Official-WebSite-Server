package org.ject.support.common.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    private AuditDummyChildEntity(String label) {
        this.label = label;
    }

    static AuditDummyChildEntity create(String label) {
        return new AuditDummyChildEntity(label);
    }

    void relabel(String label) {
        this.label = label;
    }
}
