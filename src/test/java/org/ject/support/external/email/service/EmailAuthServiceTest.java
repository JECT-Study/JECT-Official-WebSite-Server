package org.ject.support.external.email.service;

import org.ject.support.base.UnitTestSupport;
import org.ject.support.external.email.domain.EmailTemplate;
import org.ject.support.external.email.exception.RateLimitException;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

class EmailAuthServiceTest extends UnitTestSupport {

    @InjectMocks
    private EmailAuthService emailAuthService;

    @Mock
    private SesEmailSendService emailSendService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    void 인증번호_발송_시_Rate_Limit이_적용되어_있지_않다면_정상_발송된다() {
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
    void _3분_내에_재요청_시_RateLimitException이_발생한다() {
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
    void PIN_재설정_메일은_Rate_Limit_영향을_받지_않는다() {
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
