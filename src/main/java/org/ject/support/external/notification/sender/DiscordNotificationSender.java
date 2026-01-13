package org.ject.support.external.notification.sender;

import lombok.RequiredArgsConstructor;
import org.ject.support.external.notification.client.DiscordClient;
import org.ject.support.external.notification.payload.DiscordWebhookPayload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Primary
@Component
@RequiredArgsConstructor
public class DiscordNotificationSender implements NotificationSender {

    private final DiscordClient discordClient;
    private final Environment environment;

    @Value("${notification.discord.webhook.admin-login}")
    private String adminLoginWebhook;

    @Value("${notification.discord.webhook.supporter-token-issue}")
    private String supporterTokenIssueWebhook;

    @Override
    public Mono<Void> sendAdminLogin(
            String email,
            String code
    ) {
        return discordClient.send(
                adminLoginWebhook,
                DiscordWebhookPayload
                        .adminLogin(email, code)
                        .withPrefix(resolvePrefix())
        );
    }

    @Override
    public Mono<Void> sendSupporterTokenIssued(
            String email,
            String accessToken
    ) {
        return discordClient.send(
                supporterTokenIssueWebhook,
                DiscordWebhookPayload
                        .supporterToken(email, accessToken)
                        .withPrefix(resolvePrefix())
        );
    }

    private String resolvePrefix() {
        if (environment.acceptsProfiles(Profiles.of("prod", "production"))) {
            return "";
        } else {
            return "[개발] ";
        }
    }
}
