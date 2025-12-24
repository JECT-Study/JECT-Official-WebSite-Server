package org.ject.support.external.email.service;

import org.ject.support.external.email.domain.EmailTemplate;
import org.ject.support.external.email.exception.RateLimitException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailAuthServiceTest {

    @InjectMocks
    private EmailAuthService emailAuthService;

    @Mock
    private SesEmailSendService emailSendService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    @DisplayName("인증번호 발송 시 Rate Limit이 적용되어 있지 않다면 정상 발송된다")
    void sendAuthCode_success() {
        // given
        String email = "test@example.com";

        given(redisTemplate.hasKey(anyString())).willReturn(false);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        // when
        emailAuthService.sendAuthCode(EmailTemplate.AUTH_CODE, email);

        // then
        // 1. Rate Limit 키가 설정되었는지 검증 (3분)
        verify(valueOperations).set(eq("email:rate_limit:" + email), eq("1"), eq(Duration.ofMinutes(3)));
        // 2. 이메일 발송이 호출되었는지 검증
        verify(emailSendService).sendTemplatedEmail(eq(EmailTemplate.AUTH_CODE), eq(email), any());
    }

    @Test
    @DisplayName("3분 내에 재요청 시 RateLimitException이 발생한다")
    void sendAuthCode_fail_rate_limit() {
        // given
        String email = "test@example.com";
        given(redisTemplate.hasKey(anyString())).willReturn(true); // 이미 키가 존재함
        given(redisTemplate.getExpire(anyString())).willReturn(120L); // 120초 남음

        // when, then
        assertThatThrownBy(() -> emailAuthService.sendAuthCode(EmailTemplate.AUTH_CODE, email))
                .isInstanceOf(RateLimitException.class)
                .extracting("retryAfter")
                .isEqualTo(120L);

        // 이메일 발송은 호출되지 않아야 함
        verify(emailSendService, never()).sendTemplatedEmail(any(), any(), any());
    }

    @Test
    @DisplayName("PIN 재설정 메일은 Rate Limit 영향을 받지 않는다")
    void sendAuthCode_pin_reset_ignore_limit() {
        // given
        String email = "test@example.com";
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        // when
        emailAuthService.sendAuthCode(EmailTemplate.PIN_RESET, email);

        // then
        // Rate Limit 체크(hasKey)를 하지 않아야 함
        verify(redisTemplate, never()).hasKey(anyString());

        // 이메일 발송은 정상 호출
        verify(emailSendService).sendTemplatedEmail(eq(EmailTemplate.PIN_RESET), eq(email), any());
    }
}
