package org.ject.support.external.email.dto;

import org.ject.support.external.email.domain.EmailTemplate;

import java.util.Map;

public record SendManualTemplatedEmailRequest(
        EmailTemplate sendGroupCode,
        String to,
        Map<String, String> content) {
}
