package org.ject.support.external.discord;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DiscordWebhookPayload {
    private String content;
    private boolean tts = false;
    private List<Embed> embeds = new ArrayList<>();

    @Data
    @AllArgsConstructor
    public static class Embed {
        private String title;
        private String description;
    }
}
