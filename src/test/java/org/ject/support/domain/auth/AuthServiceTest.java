package org.ject.support.domain.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.ject.support.domain.auth.AuthErrorCode.INVALID_AUTH_CODE;
import static org.ject.support.domain.auth.AuthErrorCode.NOT_FOUND_AUTH_CODE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.ject.support.common.security.jwt.JwtTokenProvider;
import org.ject.support.domain.auth.AuthDto.AuthCodeResponse;
import org.ject.support.domain.member.Member;
import org.ject.support.domain.member.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private final String TEST_NAME = "test";
    private final String TEST_EMAIL = "test@example.com";
    private final String TEST_PHONE_NUMBER = "01012345678";
    private final String TEST_AUTH_CODE = "123456";
    private final String TEST_ACCESS_TOKEN = "test.access.token";
    private final String TEST_REFRESH_TOKEN = "test.refresh.token";

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("이메일 인증 코드 검증 성공 - 신규 회원")
    void verifyEmailByAuthCode_NewMember_Success() {
        // given
        Authentication authentication = mock(Authentication.class);
        Member newMember = Member.builder()
                .name(TEST_NAME)
                .email(TEST_EMAIL)
                .phoneNumber(TEST_PHONE_NUMBER)
                .build();

        given(valueOperations.get(TEST_EMAIL)).willReturn(TEST_AUTH_CODE);
        given(memberRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.empty());
        given(memberRepository.save(any(Member.class))).willReturn(newMember);
        given(jwtTokenProvider.createAuthenticationByMember(any(Member.class))).willReturn(authentication);
        given(jwtTokenProvider.createAccessToken(any(), any())).willReturn(TEST_ACCESS_TOKEN);
        given(jwtTokenProvider.createRefreshToken(any())).willReturn(TEST_REFRESH_TOKEN);

        // when
        AuthCodeResponse result = authService.verifyEmailByAuthCode(TEST_NAME, TEST_EMAIL,
                TEST_PHONE_NUMBER, TEST_AUTH_CODE);

        // then
        assertThat(result.accessToken()).isEqualTo(TEST_ACCESS_TOKEN);
        assertThat(result.refreshToken()).isEqualTo(TEST_REFRESH_TOKEN);
        verify(redisTemplate).delete(TEST_EMAIL);
    }

    @Test
    @DisplayName("이메일 인증 코드 검증 성공 - 기존 회원")
    void verifyEmailByAuthCode_ExistingMember_Success() {
        // given
        Authentication authentication = mock(Authentication.class);
        Member existingMember = Member.builder()
            .email(TEST_EMAIL)
            .build();

        given(valueOperations.get(TEST_EMAIL)).willReturn(TEST_AUTH_CODE);
        given(memberRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(existingMember));
        given(jwtTokenProvider.createAuthenticationByMember(any(Member.class))).willReturn(authentication);
        given(jwtTokenProvider.createAccessToken(any(), any())).willReturn(TEST_ACCESS_TOKEN);
        given(jwtTokenProvider.createRefreshToken(any())).willReturn(TEST_REFRESH_TOKEN);

        // when
        AuthCodeResponse result = authService.verifyEmailByAuthCode(TEST_NAME, TEST_EMAIL,
                TEST_PHONE_NUMBER, TEST_AUTH_CODE);
        // then
        assertThat(result.accessToken()).isEqualTo(TEST_ACCESS_TOKEN);
        assertThat(result.refreshToken()).isEqualTo(TEST_REFRESH_TOKEN);
        verify(redisTemplate).delete(TEST_EMAIL);
    }

    @Test
    @DisplayName("인증 코드 검증 실패 - 잘못된 코드")
    void verifyAuthCode_InvalidCode_ThrowsException() {
        // given
        String wrongCode = "wrong_code";
        given(valueOperations.get(anyString())).willReturn(TEST_AUTH_CODE);

        // when & then
        assertThatThrownBy(() -> authService.verifyAuthCode(TEST_EMAIL, wrongCode))
            .isInstanceOf(AuthException.class)
            .extracting(e -> ((AuthException) e).getErrorCode())
            .isEqualTo(INVALID_AUTH_CODE);
    }

    @Test
    @DisplayName("인증 코드 검증 실패 - 만료된 코드")
    void verifyAuthCode_ExpiredCode_ThrowsException() {
        // given
        given(valueOperations.get(anyString())).willReturn(null);

        // when & then
        assertThatThrownBy(() -> authService.verifyAuthCode(TEST_EMAIL, TEST_AUTH_CODE))
            .isInstanceOf(AuthException.class)
            .extracting(e -> ((AuthException) e).getErrorCode())
            .isEqualTo(NOT_FOUND_AUTH_CODE);
    }
}
