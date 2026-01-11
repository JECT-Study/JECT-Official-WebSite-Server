package org.ject.support.external.n8n.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ject.support.domain.admin.dto.SubmittedApplyDetailResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class N8nClient {

    private final WebClient webClient;

    @Value("${n8n.secret-key}")
    private String secretKey;

    @Value("${n8n.webhook.application-submit}")
    private String applicationSubmitWebhook;

    public void send(SubmittedApplyDetailResponse payload) {
        webClient.post()
                .uri(applicationSubmitWebhook)
                .header("Apply-Webhook-Secret", secretKey)
                .bodyValue(payload)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(this::logSuccess)
                .doOnError(this::logError)
                .block();
    }

    private void logSuccess(ResponseEntity<Void> response) {
        log.info("N8n message sent successfully (status={})", response.getStatusCode());
    }

    private void logError(Throwable e) {
        log.error(
                "N8n message send failed apply: {} - {}",
                e.getClass().getSimpleName(),
                e.getMessage()
        );
    }
}
