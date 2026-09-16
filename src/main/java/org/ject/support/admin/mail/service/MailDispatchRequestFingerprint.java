package org.ject.support.admin.mail.service;

import java.util.Map;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.mail.dto.SendMailDispatchRequest;
import org.ject.support.common.util.Map2JsonSerializer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailDispatchRequestFingerprint {

    private final Map2JsonSerializer map2JsonSerializer;

    public String generate(SendMailDispatchRequest request) {
        Map<String, Object> payload = new TreeMap<>();
        payload.put("applyIds", request.applyIds());
        payload.put("inputVariables", request.inputVariables() == null
                ? Map.of()
                : new TreeMap<>(request.inputVariables()));
        payload.put("recruitId", request.recruitId());
        payload.put("scenarioId", request.scenarioId());
        payload.put("subjectOverride", request.subjectOverride());
        return map2JsonSerializer.serializeAsString(payload);
    }
}
