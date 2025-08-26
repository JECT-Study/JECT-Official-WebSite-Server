package org.ject.support.external.email.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ject.support.common.util.CodeGeneratorUtil;
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

    private final SesEmailSendService emailSendService;
    private final RedisTemplate<String, String> redisTemplate;

    public void sendAuthCode(String sendGroupCode, String toAddress) {
        String authCode = CodeGeneratorUtil.generateAuthCode(AUTH_CODE_LENGTH);
        sendAuthCodeEmail(sendGroupCode, toAddress, authCode);
        storeAuthCode(toAddress, authCode);
    }

    private void sendAuthCodeEmail(String sendGroupCode, String toAddress, String authCode) {
        emailSendService.sendTemplatedEmail(sendGroupCode, toAddress, Map.of("auth-code", authCode));
        log.info("인증 번호 전송 - email: {}, code: {}", toAddress, authCode);
    }

    private void storeAuthCode(String toAddress, String authCode) {
        redisTemplate.opsForValue().set(toAddress, authCode, Duration.ofSeconds(EXPIRE_TIME));
    }
}
