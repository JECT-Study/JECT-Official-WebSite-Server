package org.ject.support.domain.admin.dto;

import lombok.Builder;

@Builder
public record AdminVerifySlackResponse(String id, String email, String name) {
}
