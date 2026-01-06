package org.ject.support.external.notification.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ject.support.external.notification.payload.DiscordWebhookPayload;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordClient {

    private final WebClient webClient;

    public Mono<Void> send(
            String webhookUrl,
            DiscordWebhookPayload payload
    ) {
        if (!StringUtils.hasText(webhookUrl)) {
            log.warn("Discord webhook URL이 비어있어 메시지를 전송하지 않습니다.");
            return Mono.empty();
        }

        return webClient.post()
                .uri(webhookUrl)
                .bodyValue(payload)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(this::logSuccess)
                .doOnError(this::logError)
                .then();
    }

    private void logSuccess(ResponseEntity<Void> response) {
        log.info("Discord message sent successfully (status={})", response.getStatusCode());
    }

    private void logError(Throwable e) {
        log.error(
                "Discord message send failed: {} - {}",
                e.getClass().getSimpleName(),
                e.getMessage()
        );
    }
}
