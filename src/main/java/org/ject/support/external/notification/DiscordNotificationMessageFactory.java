package org.ject.support.external.notification;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class DiscordNotificationMessageFactory implements NotificationMessageFactory {

    @Override
    public String adminLoginCode(String email, String code) {
        return """
               관리자 로그인 인증 코드 요청
               관리자 이메일: %s
               인증 코드 : ||%s||
               """.formatted(email, code);
    }

    @Override
    public String supporterAccessTokenIssued(String email, String accessToken) {
        return """
               서포터즈 엑세스 토큰 요청
               요청 이메일 : %s
               토큰 : ||%s||
               """.formatted(email, accessToken);
    }
}
