package org.ject.support.external.notification.payload;

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

    public DiscordWebhookPayload withPrefix(String prefix) {
        DiscordWebhookPayload copy = new DiscordWebhookPayload();
        copy.setContent(prefix + this.content);
        copy.getEmbeds().addAll(this.embeds);
        return copy;
    }

    public static DiscordWebhookPayload adminLogin(
            String email,
            String code
    ) {
        DiscordWebhookPayload payload = new DiscordWebhookPayload();
        payload.setContent("[관리자 로그인]");

        payload.getEmbeds().add(
                new Embed(
                        "관리자 로그인 인증 코드 요청",
                        """
                        이메일: %s
                        인증 코드 : ||%s||
                        """.formatted(email, code)
                )
        );
        return payload;
    }

    public static DiscordWebhookPayload supporterToken(
            String email,
            String accessToken
    ) {
        DiscordWebhookPayload payload = new DiscordWebhookPayload();
        payload.setContent("[서포터즈 임시 토큰 발급]");

        payload.getEmbeds().add(
                new Embed(
                        "서포터즈 토큰 발급",
                        """
                        이메일: %s
                        토큰 : ||%s||
                        """.formatted(email, accessToken)
                )
        );
        return payload;
    }
}
