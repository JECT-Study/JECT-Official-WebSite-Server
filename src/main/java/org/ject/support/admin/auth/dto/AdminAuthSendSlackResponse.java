package org.ject.support.admin.auth.dto;

import lombok.Builder;

@Builder
public record AdminAuthSendSlackResponse(String email) {
}
