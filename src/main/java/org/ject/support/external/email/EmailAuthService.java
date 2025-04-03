package org.ject.support.external.email;

import java.security.SecureRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailAuthService {

    private static final int AUTH_CODE_LENGTH = 6;
    private static final long EXPIRE_TIME = 300L; // 5분

    private final RedisTemplate<String, String> redisTemplate;
    private final EmailSendService emailSendService;

    public void sendAuthCode(String email, EmailTemplate template) {
        String authCode = generateAuthCode();
        storeAuthCode(email, authCode);
        sendAuthCodeEmail(email, authCode, template);
    }

    private String generateAuthCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < AUTH_CODE_LENGTH; i++) {
            builder.append(random.nextInt(10));
        }
        return builder.toString();
    }

    private void storeAuthCode(String email, String authCode) {
        redisTemplate.opsForValue().set(email, authCode, Duration.ofSeconds(EXPIRE_TIME));
        log.info("인증 번호 전송 - email: {}, code: {}", email, authCode);
    }

    private void sendAuthCodeEmail(String email, String authCode, EmailTemplate template) {
        // TODO: 정책 추가 시 인증 번호 전송 횟수 제한 로직 추가
        emailSendService.sendEmail(
            email,
            template,
            Map.of("to", email, "value", authCode)
        );
    }
}
