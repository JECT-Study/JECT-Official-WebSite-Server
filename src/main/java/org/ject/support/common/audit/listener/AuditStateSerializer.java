package org.ject.support.common.audit.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.CollectionType;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;

import java.util.LinkedHashMap;
import java.util.Map;

final class AuditStateSerializer {

    static final Object EXCLUDED = new Object();

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private AuditStateSerializer() {
    }

    static Map<String, Object> toStateMap(String[] propertyNames, Object[] state, Type[] propertyTypes,
        SharedSessionContractImplementor session) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (state == null) {
            return map;
        }
        for (int i = 0; i < propertyNames.length; i++) {
            Object normalized = normalize(state[i], propertyTypes == null ? null : propertyTypes[i], session);
            if (normalized == EXCLUDED) {
                continue;
            }
            map.put(propertyNames[i], normalized);
        }
        return map;
    }

    static Object normalize(Object value, Type type, SharedSessionContractImplementor session) {
        if (type instanceof CollectionType) {
            return EXCLUDED;
        }
        if (type instanceof EntityType entityType && value != null) {
            return entityType.getAssociatedEntityPersister(session.getFactory()).getIdentifier(value, session);
        }
        return value;
    }

    static int indexOf(String[] propertyNames, String target) {
        for (int i = 0; i < propertyNames.length; i++) {
            if (propertyNames[i].equals(target)) {
                return i;
            }
        }
        return -1;
    }

    static String writeJson(Map<String, Object> state) {
        try {
            return OBJECT_MAPPER.writeValueAsString(state);
        } catch (Exception e) {
            throw new IllegalStateException("변경이력 상태 직렬화에 실패했습니다.", e);
        }
    }
}
