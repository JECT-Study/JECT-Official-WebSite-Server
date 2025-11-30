package org.ject.support.external.discord;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordComponent {

    private final WebClient webClient;
    private final Environment environment;

    @Value("${notification.discord.webhook.admin-login}")
    private String adminLoginWebhook;

    public void sendAdminLoginMessage(String description) {
        DiscordWebhookPayload.Embed content = new DiscordWebhookPayload.Embed("로그인 인증 요청", description);
        DiscordWebhookPayload payload = new DiscordWebhookPayload();
        payload.setContent("[관리자 로그인]");
        payload.getEmbeds().add(content);
        sendMessage(adminLoginWebhook, payload);
    }

    public void sendMessage(String webhookUrl, DiscordWebhookPayload payload) {
        if (isBlank(webhookUrl)) {
            log.warn("Discord Webhook URL이 설정되지 않았습니다.");
            return;
        }
        try {
            String prefix = getPrefix();
            String originalContent = payload.getContent() == null ? "" : payload.getContent();
            payload.setContent(prefix + originalContent);

            webClient.post()
                    .uri(webhookUrl)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

        } catch (Exception e) {
            log.error("Discord Message 전송 Error: {}", e.getMessage());
        }
    }

    private String getPrefix() {
        return isProduction() ? "" : "[개발] ";
    }

    private boolean isProduction() {
        return environment.acceptsProfiles(Profiles.of("prod", "production"));
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
