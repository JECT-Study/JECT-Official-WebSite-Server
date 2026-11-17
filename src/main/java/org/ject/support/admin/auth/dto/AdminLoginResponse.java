package org.ject.support.admin.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
public record AdminLoginResponse(
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String name,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        boolean isAdmin
) {
}
