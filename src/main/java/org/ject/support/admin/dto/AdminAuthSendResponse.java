package org.ject.support.admin.dto;

import lombok.Builder;

@Builder
public record AdminAuthSendResponse(String email) {
}
