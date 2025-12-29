package org.ject.support.external.discord;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class DiscordComponent {

    private final WebClient webClient;
    private final Environment environment;
    private final String prefix;

    @Value("${notification.discord.webhook.admin-login}")
    private String adminLoginWebhook;

    public DiscordComponent(WebClient webClient, Environment environment) {
        this.webClient = webClient;
        this.environment = environment;
        this.prefix = resolvePrefix(environment);
    }

    public Mono<Void> sendAdminLoginMessage(String description) {
        DiscordWebhookPayload.Embed content = new DiscordWebhookPayload.Embed("로그인 인증 요청", description);
        DiscordWebhookPayload payload = new DiscordWebhookPayload();
        payload.setContent("[관리자 로그인]");
        payload.getEmbeds().add(content);
        return sendMessage(adminLoginWebhook, payload);
    }

    /**
     * 논블로킹 방식으로 메시지 전송. 호출자가 구독/에러처리를 담당해야 함.
     * 기존 payload는 변경하지 않고 사본을 만들어 전송.
     */
    public Mono<Void> sendMessage(String webhookUrl, DiscordWebhookPayload payload) {
        if (!StringUtils.hasText(webhookUrl)) {
            log.warn("Discord webhook URL이 설정되지 않았습니다, 메시지 전송을 건너뜁니다.");
            return Mono.empty();
        }

        DiscordWebhookPayload toSend = createPayloadCopyWithPrefix(payload, prefix);

        return webClient.post()
                .uri(webhookUrl)
                .bodyValue(toSend)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess((ResponseEntity<Void> resp) ->
                        log.info("Discord message sent successfully"))
                .doOnError(e ->
                        log.error("Discord message send failed: {} - {}", e.getClass().getSimpleName(), e.getMessage()))
                .then();
    }

    private DiscordWebhookPayload createPayloadCopyWithPrefix(DiscordWebhookPayload original, String prefix) {
        DiscordWebhookPayload copy = new DiscordWebhookPayload();
        String originalContent = original == null || original.getContent() == null ? "" : original.getContent();
        copy.setContent(prefix + originalContent);

        if (original != null && original.getEmbeds() != null) {
            copy.getEmbeds().addAll(original.getEmbeds());
        }
        return copy;
    }

    private String resolvePrefix(Environment environment) {
        if (environment != null && environment.acceptsProfiles(Profiles.of("prod", "production"))) {
            return "";
        } else {
            return "[개발] ";
        }
    }
}
