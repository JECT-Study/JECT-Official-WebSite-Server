package org.ject.support.external.email.dto;

import java.util.List;
import java.util.Map;

public record SendManualBulkTemplatedEmailRequest(
        String sendGroupCode,
        List<String> toList,
        Map<String, String> content) {
}
