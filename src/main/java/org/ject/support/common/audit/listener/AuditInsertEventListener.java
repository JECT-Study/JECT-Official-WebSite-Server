package org.ject.support.common.audit.listener;

import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.ject.support.common.audit.Auditable;
import org.ject.support.common.audit.AuditContext;
import org.ject.support.common.audit.AuditContextHolder;
import org.ject.support.domain.changehistory.domain.ChangeActionType;
import org.ject.support.domain.changehistory.domain.ChangeDetail;

import java.util.Map;

public class AuditInsertEventListener implements PostInsertEventListener {

    @Override
    public void onPostInsert(PostInsertEvent event) {
        AuditContext context = AuditContextHolder.get();
        if (context == null) {
            return;
        }

        Object entity = event.getEntity();
        if (!(entity instanceof Auditable)) {
            return;
        }

        String[] propertyNames = event.getPersister().getPropertyNames();
        Map<String, Object> afterState = AuditStateSerializer.toStateMap(propertyNames, event.getState());

        context.addDetail(ChangeDetail.create(
            entity.getClass().getSimpleName(),
            String.valueOf(event.getId()),
            ChangeActionType.CREATE,
            null,
            AuditStateSerializer.writeJson(afterState)
        ));
    }

    @Override
    public boolean requiresPostCommitHandling(EntityPersister persister) {
        return false;
    }
}
