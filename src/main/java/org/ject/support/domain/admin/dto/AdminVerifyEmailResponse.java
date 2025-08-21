package org.ject.support.domain.admin.dto;

import lombok.Builder;

@Builder
public record AdminVerifyEmailResponse(String email) {
}
