package org.ject.support.common.audit.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.LinkedHashMap;
import java.util.Map;

final class AuditStateSerializer {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private AuditStateSerializer() {
    }

    static Map<String, Object> toStateMap(String[] propertyNames, Object[] state) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (state == null) {
            return map;
        }
        for (int i = 0; i < propertyNames.length; i++) {
            map.put(propertyNames[i], state[i]);
        }
        return map;
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
