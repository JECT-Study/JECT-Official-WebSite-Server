package org.ject.support.external.slack;

import com.slack.api.Slack;
import com.slack.api.webhook.Payload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.core.env.Environment;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlackComponent {

    private final Slack slack;
    private final SlackConfig slackConfig;
    private final Environment environment;

    public void sendAdminLoginMessage(String message) {
        sendSlackMessage(message, slackConfig.getAdminLoginWebhook());
    }

    private void sendSlackMessage(String message, String webhookUri) {
        try {
            String prefix = isProduction() ? "" : "[개발] ";
            String markedMessage = prefix + message;

            Payload payload = Payload.builder()
                    .text(markedMessage.trim())
                    .build();

            slack.send(webhookUri, payload);
        } catch (Exception e) {
            log.error("Slack Message 전송 Error: {}", e.getMessage());
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
