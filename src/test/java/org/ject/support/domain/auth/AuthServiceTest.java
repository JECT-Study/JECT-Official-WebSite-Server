package org.ject.support.domain.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.ject.support.domain.auth.AuthErrorCode.INVALID_AUTH_CODE;
import static org.ject.support.domain.auth.AuthErrorCode.NOT_FOUND_AUTH_CODE;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import org.ject.support.common.security.jwt.JwtTokenProvider;
import org.ject.support.domain.auth.AuthDto.VerifyAuthCodeOnlyResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private JwtTokenProvider jwtTokenProvider;
    
    @Mock
    private ValueOperations<String, String> valueOperations;

    private final String TEST_EMAIL = "test@example.com";
    private final String TEST_AUTH_CODE = "123456";
    private final String TEST_VERIFICATION_TOKEN = "test.verification.token";

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("이메일 인증 코드 검증 성공 - 인증 토큰 발급")
    void verifyEmailByAuthCodeOnly_Success() {
        // given
        given(valueOperations.get(TEST_EMAIL)).willReturn(TEST_AUTH_CODE);
        given(jwtTokenProvider.createVerificationToken(TEST_EMAIL)).willReturn(TEST_VERIFICATION_TOKEN);

        // when
        VerifyAuthCodeOnlyResponse result = authService.verifyEmailByAuthCodeOnly(TEST_EMAIL, TEST_AUTH_CODE);

        // then
        assertThat(result.verificationToken()).isEqualTo(TEST_VERIFICATION_TOKEN);
        // 인증 코드 검증 후에는 Redis에서 코드를 삭제하지 않음 (이전과 다른 점)
    }

    @Test
    @DisplayName("인증 코드 검증 실패 - 잘못된 코드")
    void verifyEmailByAuthCodeOnly_InvalidCode_ThrowsException() {
        // given
        String wrongCode = "wrong_code";
        given(valueOperations.get(anyString())).willReturn(TEST_AUTH_CODE);

        // when & then
        assertThatThrownBy(() -> authService.verifyEmailByAuthCodeOnly(TEST_EMAIL, wrongCode))
            .isInstanceOf(AuthException.class)
            .extracting(e -> ((AuthException) e).getErrorCode())
            .isEqualTo(INVALID_AUTH_CODE);
    }

    @Test
    @DisplayName("인증 코드 검증 실패 - 만료된 코드")
    void verifyEmailByAuthCodeOnly_ExpiredCode_ThrowsException() {
        // given
        given(valueOperations.get(anyString())).willReturn(null);

        // when & then
        assertThatThrownBy(() -> authService.verifyEmailByAuthCodeOnly(TEST_EMAIL, TEST_AUTH_CODE))
            .isInstanceOf(AuthException.class)
            .extracting(e -> ((AuthException) e).getErrorCode())
            .isEqualTo(NOT_FOUND_AUTH_CODE);
    }
}
