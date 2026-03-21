package org.ject.support.external.notification.listener;

import lombok.RequiredArgsConstructor;
import org.ject.support.external.notification.sender.NotificationSender;
import org.ject.support.external.notification.event.SupporterTokenIssuedEvent;
import org.ject.support.external.notification.executor.NotificationExecutorService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationSender notificationSender;
    private final NotificationExecutorService notificationExecutorService;

    @EventListener
    public void handle(SupporterTokenIssuedEvent event) {
        notificationExecutorService.execute(
                notificationSender.sendSupporterTokenIssued(
                        event.email(),
                        event.accessToken()
                )
        );
    }
}
