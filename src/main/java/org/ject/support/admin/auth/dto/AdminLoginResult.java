package org.ject.support.admin.auth.dto;

import org.springframework.security.core.Authentication;

public record AdminLoginResult(
        Authentication authentication,
        String name,
        boolean isAdmin
) {
}
