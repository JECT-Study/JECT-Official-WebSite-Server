package org.ject.support.external.notification.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@Slf4j
public class NotificationExecutorService {

    public void execute(Mono<Void> mono) {
        mono
            .retry(3)
            .timeout(Duration.ofSeconds(3))
            .doOnError(e -> log.error("Notification failed", e))
            .subscribe();
    }
}
