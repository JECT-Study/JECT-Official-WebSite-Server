package org.ject.support.external.email.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ject.support.common.util.CodeGeneratorUtil;
import org.ject.support.external.email.domain.EmailTemplate;
import org.ject.support.external.email.exception.EmailErrorCode;
import org.ject.support.external.email.exception.RateLimitException;
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
    private static final String RATE_LIMIT_KEY_PREFIX = "email:rate_limit:";
    private static final long RATE_LIMIT_DURATION_MINUTES = 3;

    private final SesEmailSendService emailSendService;
    private final RedisTemplate<String, String> redisTemplate;

    public void sendAuthCode(EmailTemplate sendGroupCode, String toEmail) {
        checkRateLimit(sendGroupCode, toEmail);

        String authCode = CodeGeneratorUtil.generateDigitCode(AUTH_CODE_LENGTH);
        sendAuthCodeEmail(sendGroupCode, toEmail, authCode);
        storeAuthCode(toEmail, authCode);
    }

    private void checkRateLimit(EmailTemplate sendGroupCode, String toEmail) {
        if (sendGroupCode != EmailTemplate.AUTH_CODE) {
            return;
        }

        String key = RATE_LIMIT_KEY_PREFIX + toEmail;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            Long retryAfter = redisTemplate.getExpire(key);
            long seconds = retryAfter > 0 ? retryAfter : RATE_LIMIT_DURATION_MINUTES * 60;
            throw new RateLimitException(seconds);
        }

        redisTemplate.opsForValue().set(key, "1", Duration.ofMinutes(RATE_LIMIT_DURATION_MINUTES));
    }

    private void sendAuthCodeEmail(EmailTemplate sendGroupCode, String toEmail, String authCode) {
        emailSendService.sendTemplatedEmail(sendGroupCode, toEmail, Map.of("auth-code", authCode));
        log.info("인증 번호 전송 - toEmail: {}, code: {}", toEmail, authCode);
    }

    private void storeAuthCode(String toEmail, String authCode) {
        redisTemplate.opsForValue().set(toEmail, authCode, Duration.ofSeconds(EXPIRE_TIME));
    }
}
