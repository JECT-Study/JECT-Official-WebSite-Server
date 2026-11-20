package org.ject.support.common.audit;

import org.springframework.data.jpa.repository.JpaRepository;

interface AuditDummyEntityRepository extends JpaRepository<AuditDummyEntity, Long> {
}
