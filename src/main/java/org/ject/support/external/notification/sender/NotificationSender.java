package org.ject.support.external.notification.sender;

import reactor.core.publisher.Mono;

public interface NotificationSender {

    Mono<Void> sendAdminLogin(
            String email,
            String code
    );

    Mono<Void> sendSupporterTokenIssued(
            String email,
            String accessToken
    );
}
