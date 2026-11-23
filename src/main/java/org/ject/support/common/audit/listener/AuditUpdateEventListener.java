package org.ject.support.common.audit.listener;

import org.hibernate.event.spi.PreUpdateEvent;
import org.hibernate.event.spi.PreUpdateEventListener;
import org.hibernate.type.Type;
import org.ject.support.common.audit.Auditable;
import org.ject.support.common.audit.AuditContext;
import org.ject.support.common.audit.AuditContextHolder;
import org.ject.support.common.audit.SoftDeletable;
import org.ject.support.domain.changehistory.domain.ChangeActionType;
import org.ject.support.domain.changehistory.domain.ChangeDetail;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class AuditUpdateEventListener implements PreUpdateEventListener {

    private static final String DELETED_FLAG_PROPERTY = "isDeleted";

    @Override
    public boolean onPreUpdate(PreUpdateEvent event) {
        AuditContext context = AuditContextHolder.get();
        if (context == null) {
            return false;
        }

        Object entity = event.getEntity();
        if (!(entity instanceof Auditable)) {
            return false;
        }

        String[] propertyNames = event.getPersister().getPropertyNames();
        Type[] propertyTypes = event.getPersister().getPropertyTypes();
        Object[] oldState = event.getOldState();
        Object[] newState = event.getState();

        Map<String, Object> before = new LinkedHashMap<>();
        Map<String, Object> after = new LinkedHashMap<>();
        for (int i = 0; i < propertyNames.length; i++) {
            Type propertyType = propertyTypes == null ? null : propertyTypes[i];
            Object oldValue = AuditStateSerializer.normalize(
                oldState == null ? null : oldState[i], propertyType, event.getSession());
            Object newValue = AuditStateSerializer.normalize(newState[i], propertyType, event.getSession());
            if (oldValue == AuditStateSerializer.EXCLUDED || newValue == AuditStateSerializer.EXCLUDED) {
                continue;
            }
            if (!Objects.equals(oldValue, newValue)) {
                before.put(propertyNames[i], oldValue);
                after.put(propertyNames[i], newValue);
            }
        }

        if (before.isEmpty()) {
            return false;
        }

        ChangeActionType actionType = resolveActionType(entity, propertyNames, oldState, newState);

        context.addDetail(ChangeDetail.create(
            entity.getClass().getSimpleName(),
            String.valueOf(event.getId()),
            actionType,
            AuditStateSerializer.writeJson(before),
            AuditStateSerializer.writeJson(after)
        ));

        return false;
    }

    private ChangeActionType resolveActionType(Object entity, String[] propertyNames, Object[] oldState, Object[] newState) {
        if (!(entity instanceof SoftDeletable)) {
            return ChangeActionType.UPDATE;
        }

        int index = AuditStateSerializer.indexOf(propertyNames, DELETED_FLAG_PROPERTY);
        if (index < 0) {
            return ChangeActionType.UPDATE;
        }

        boolean wasDeleted = Boolean.TRUE.equals(oldState == null ? null : oldState[index]);
        boolean isDeleted = Boolean.TRUE.equals(newState[index]);

        if (!wasDeleted && isDeleted) {
            return ChangeActionType.DELETE;
        }
        if (wasDeleted && !isDeleted) {
            return ChangeActionType.RESTORE;
        }
        return ChangeActionType.UPDATE;
    }
}
