package org.ject.support.common.audit.listener;

import org.ject.support.common.audit.Auditable;
import org.ject.support.common.audit.SoftDeletable;

final class AuditListenerTestFixtures {

    private AuditListenerTestFixtures() {
    }

    static class AuditableEntity implements Auditable {
    }

    static class AuditableSoftDeletableEntity implements Auditable, SoftDeletable {
    }

    static class NonAuditableEntity {
    }
}
