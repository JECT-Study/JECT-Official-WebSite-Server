package org.ject.support.external.email.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailAuthService {

    private static final int AUTH_CODE_LENGTH = 6;
    private static final long EXPIRE_TIME = 300L; // 5분

    private final SesEmailSendService emailSendService;
    private final RedisTemplate<String, String> redisTemplate;

    public void sendAuthCode(String sendGroupCode, String toAddress) {
        String authCode = generateAuthCode();
        sendAuthCodeEmail(sendGroupCode, toAddress, authCode);
        storeAuthCode(toAddress, authCode);
    }

    private String generateAuthCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < AUTH_CODE_LENGTH; i++) {
            builder.append(random.nextInt(10));
        }
        return builder.toString();
    }

    private void sendAuthCodeEmail(String sendGroupCode, String toAddress, String authCode) {
        emailSendService.sendTemplatedEmail(sendGroupCode, toAddress, Map.of("auth-code", authCode));
        log.info("인증 번호 전송 - email: {}, code: {}", toAddress, authCode);
    }

    private void storeAuthCode(String toAddress, String authCode) {
        redisTemplate.opsForValue().set(toAddress, authCode, Duration.ofSeconds(EXPIRE_TIME));
    }
}
