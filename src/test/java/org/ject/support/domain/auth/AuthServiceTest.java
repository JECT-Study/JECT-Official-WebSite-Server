package org.ject.support.domain.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.ject.support.domain.auth.AuthErrorCode.EXPIRED_REFRESH_TOKEN;
import static org.ject.support.domain.auth.AuthErrorCode.INVALID_AUTH_CODE;
import static org.ject.support.domain.auth.AuthErrorCode.INVALID_REFRESH_TOKEN;
import static org.ject.support.domain.auth.AuthErrorCode.NOT_FOUND_AUTH_CODE;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.ject.support.common.security.jwt.JwtTokenProvider;
import org.ject.support.domain.auth.AuthDto.TokenRefreshResponse;
import org.ject.support.domain.auth.AuthDto.VerifyAuthCodeOnlyResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
    private final String TEST_REFRESH_TOKEN = "test.refresh.token";
    private final String TEST_ACCESS_TOKEN = "test.access.token";
    private final Long TEST_MEMBER_ID = 1L;

    @BeforeEach
    void setUp() {
        // Mockito 설정 - 불필요한 스터빙 경고 무시
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
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
    
    @Test
    @DisplayName("리프레시 토큰 검증 성공 - 새 액세스 토큰 발급")
    void refreshAccessToken_Success() {
        // given
        given(jwtTokenProvider.validateToken(TEST_REFRESH_TOKEN)).willReturn(true);
        given(jwtTokenProvider.reissueAccessToken(TEST_REFRESH_TOKEN, TEST_MEMBER_ID)).willReturn(TEST_ACCESS_TOKEN);

        // when
        TokenRefreshResponse result = authService.refreshAccessToken(TEST_MEMBER_ID, TEST_REFRESH_TOKEN);

        // then
        assertThat(result.accessToken()).isEqualTo(TEST_ACCESS_TOKEN);
    }
    
    @Test
    @DisplayName("리프레시 토큰 검증 실패 - 유효하지 않은 토큰")
    void refreshAccessToken_InvalidToken_ThrowsException() {
        // given
        given(jwtTokenProvider.validateToken(TEST_REFRESH_TOKEN)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.refreshAccessToken(TEST_MEMBER_ID, TEST_REFRESH_TOKEN))
            .isInstanceOf(AuthException.class)
            .extracting(e -> ((AuthException) e).getErrorCode())
            .isEqualTo(INVALID_REFRESH_TOKEN);
    }
    
    @Test
    @DisplayName("리프레시 토큰 검증 실패 - 만료된 토큰")
    void refreshAccessToken_ExpiredToken_ThrowsException() {
        // given
        given(jwtTokenProvider.validateToken(TEST_REFRESH_TOKEN)).willThrow(new ExpiredJwtException(null, null, "만료된 토큰"));

        // when & then
        assertThatThrownBy(() -> authService.refreshAccessToken(TEST_MEMBER_ID, TEST_REFRESH_TOKEN))
            .isInstanceOf(AuthException.class)
            .extracting(e -> ((AuthException) e).getErrorCode())
            .isEqualTo(EXPIRED_REFRESH_TOKEN);
    }
    
    @Test
    @DisplayName("리프레시 토큰 검증 실패 - JWT 예외 발생")
    void refreshAccessToken_JwtException_ThrowsException() {
        // given
        given(jwtTokenProvider.validateToken(TEST_REFRESH_TOKEN)).willThrow(JwtException.class);

        // when & then
        assertThatThrownBy(() -> authService.refreshAccessToken(TEST_MEMBER_ID, TEST_REFRESH_TOKEN))
            .isInstanceOf(AuthException.class)
            .extracting(e -> ((AuthException) e).getErrorCode())
            .isEqualTo(INVALID_REFRESH_TOKEN);
    }
}
