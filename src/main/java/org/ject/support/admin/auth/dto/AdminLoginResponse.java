package org.ject.support.admin.auth.dto;

public record AdminLoginResponse(
        String name,
        boolean isAdmin
) {
}
