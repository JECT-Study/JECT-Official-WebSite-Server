package org.ject.support.external.slack;

import com.slack.api.Slack;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SlackConfig {

    @Value("${notification.slack.webhook.admin-login}")
    private String adminLoginWebhook;

    @Bean
    public Slack slack()  {
        return Slack.getInstance();
    }

    public String getAdminLoginWebhook() {
        return adminLoginWebhook;
    }
}
