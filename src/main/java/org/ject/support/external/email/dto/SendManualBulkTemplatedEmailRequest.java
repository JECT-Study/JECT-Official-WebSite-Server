package org.ject.support.external.email.dto;

import org.ject.support.external.email.domain.EmailTemplate;

import java.util.List;
import java.util.Map;

public record SendManualBulkTemplatedEmailRequest(
        EmailTemplate sendGroupCode,
        List<String> toList,
        Map<String, String> content) {
}
