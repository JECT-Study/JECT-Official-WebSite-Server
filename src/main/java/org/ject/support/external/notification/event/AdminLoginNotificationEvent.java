package org.ject.support.external.notification.event;

public record AdminLoginNotificationEvent(
        String email,
        String code
) {}
