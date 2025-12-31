package org.ject.support.external.notification;

public interface NotificationMessageFactory {
    String adminLoginCode(String email, String code);
    String supporterAccessTokenIssued(String email, String accessToken);
}
