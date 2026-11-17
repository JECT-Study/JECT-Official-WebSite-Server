package org.ject.support.common.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class AuditDummyEntity implements Auditable, SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Boolean isDeleted;

    private AuditDummyEntity(String name) {
        this.name = name;
        this.isDeleted = false;
    }

    static AuditDummyEntity create(String name) {
        return new AuditDummyEntity(name);
    }

    void rename(String name) {
        this.name = name;
    }

    void delete() {
        this.isDeleted = true;
    }

    void restore() {
        this.isDeleted = false;
    }
}
