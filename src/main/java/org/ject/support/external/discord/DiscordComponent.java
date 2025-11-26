package org.ject.support.external.discord;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordComponent {

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
        try {
            String prefix = isProduction() ? "" : "[개발] ";
            payload.setContent(prefix + payload.getContent());
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<DiscordWebhookPayload> entity = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(webhookUrl, entity, String.class);
        } catch (Exception e) {
            log.error("Discord Message 전송 Error: {}", e.getMessage());
        }
    }

    private boolean isProduction() {
        String[] profiles = environment.getActiveProfiles();
        for (String profile : profiles) {
            if (profile.equalsIgnoreCase("prod") || profile.equalsIgnoreCase("production")) {
                return true;
            }
        }
        return false;
    }
}
