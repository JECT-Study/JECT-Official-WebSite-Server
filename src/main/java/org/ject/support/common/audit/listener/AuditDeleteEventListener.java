package org.ject.support.common.audit.listener;

import org.hibernate.event.spi.PreDeleteEvent;
import org.hibernate.event.spi.PreDeleteEventListener;
import org.ject.support.common.audit.Auditable;
import org.ject.support.common.audit.AuditContext;
import org.ject.support.common.audit.AuditContextHolder;
import org.ject.support.domain.changehistory.domain.ChangeActionType;
import org.ject.support.domain.changehistory.domain.ChangeDetail;

import java.util.Map;

public class AuditDeleteEventListener implements PreDeleteEventListener {

    @Override
    public boolean onPreDelete(PreDeleteEvent event) {
        AuditContext context = AuditContextHolder.get();
        if (context == null) {
            return false;
        }

        Object entity = event.getEntity();
        if (!(entity instanceof Auditable)) {
            return false;
        }

        String[] propertyNames = event.getPersister().getPropertyNames();
        Map<String, Object> beforeState = AuditStateSerializer.toStateMap(
            propertyNames, event.getDeletedState(), event.getPersister().getPropertyTypes(), event.getSession());

        context.addDetail(ChangeDetail.create(
            entity.getClass().getSimpleName(),
            String.valueOf(event.getId()),
            ChangeActionType.DELETE,
            AuditStateSerializer.writeJson(beforeState),
            null
        ));

        return false;
    }
}
