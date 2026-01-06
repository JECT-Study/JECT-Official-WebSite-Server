package org.ject.support.external.notification.event;

public record SupporterTokenIssuedEvent(
        String email,
        String accessToken
) {}
