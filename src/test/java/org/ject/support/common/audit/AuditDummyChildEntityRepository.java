package org.ject.support.common.audit;

import org.springframework.data.jpa.repository.JpaRepository;

interface AuditDummyChildEntityRepository extends JpaRepository<AuditDummyChildEntity, Long> {
}
