package org.ject.support.external.email.dto;

import java.util.Map;

public record SendManualTemplatedEmailRequest(
        String sendGroupCode,
        String to,
        Map<String, String> content) {
}
