package org.ject.support.domain.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.ject.support.domain.auth.exception.AuthErrorCode.EXPIRED_REFRESH_TOKEN;
import static org.ject.support.domain.auth.exception.AuthErrorCode.INVALID_REFRESH_TOKEN;
import static org.ject.support.domain.auth.exception.AuthErrorCode.INVALID_CREDENTIALS;
import static org.ject.support.domain.member.exception.MemberErrorCode.NOT_FOUND_MEMBER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.ject.support.common.security.jwt.JwtTokenProvider;
import org.ject.support.domain.auth.dto.AuthVerificationResult;
import org.ject.support.domain.auth.exception.AuthException;
import org.ject.support.domain.auth.service.AuthService;
import org.ject.support.external.email.domain.EmailTemplate;
import org.ject.support.external.email.exception.EmailErrorCode;
import org.ject.support.external.email.exception.EmailException;
import static org.mockito.Mockito.lenient;

import java.util.Optional;

import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.exception.MemberException;
import org.ject.support.domain.member.repository.MemberRepository;
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
import org.springframework.security.core.Authentication;

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
    
    @Mock
    private MemberRepository memberRepository;
    
    @Mock
    private Authentication authentication;
    
    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    private final String TEST_EMAIL = "test@example.com";
    private final String TEST_AUTH_CODE = "123456";
    private final String TEST_VERIFICATION_TOKEN = "test.verification.token";
    private final String TEST_PIN = "123456";
    private final String TEST_ENCODED_PIN = "123456";
    private final String TEST_ACCESS_TOKEN = "test.access.token";
    private final String TEST_REFRESH_TOKEN = "test.refresh.token";
    private final Long TEST_MEMBER_ID = 1L;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("PIN 재설정을 위한 이메일 인증 코드 검증 성공 - 액세스 토큰 발급")
    void verifyEmailByAuthCodeOnly_PinReset_Success() {
        // given
        Member member = Member.builder()
                .id(TEST_MEMBER_ID)
                .email(TEST_EMAIL)
                .pin(TEST_ENCODED_PIN)
                .role(Role.USER)
                .build();
        
        given(valueOperations.get(TEST_EMAIL)).willReturn(TEST_AUTH_CODE);
        given(memberRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(member));
        given(jwtTokenProvider.createAuthenticationByMember(member)).willReturn(authentication);
        given(jwtTokenProvider.createAccessToken(authentication, TEST_MEMBER_ID)).willReturn(TEST_ACCESS_TOKEN);

        // when
        AuthVerificationResult result = authService.verifyAuthCodeByTemplate(TEST_EMAIL, TEST_AUTH_CODE, EmailTemplate.PIN_RESET);

        // then
        assertThat(result).isNotNull();
        assertThat(result.hasAuthentication()).isTrue();
        assertThat(result.getAuthentication()).isEqualTo(authentication);
        assertThat(result.getEmail()).isNull();
    }
    
    @Test
    @DisplayName("리프레시 토큰 검증 성공 - 새 액세스 토큰 발급")
    void refreshAccessToken_Success() {
        // given
        given(jwtTokenProvider.validateToken(TEST_REFRESH_TOKEN)).willReturn(true);
        given(jwtTokenProvider.extractMemberId(TEST_REFRESH_TOKEN)).willReturn(TEST_MEMBER_ID);
        given(jwtTokenProvider.reissueAccessToken(TEST_REFRESH_TOKEN, TEST_MEMBER_ID)).willReturn(TEST_ACCESS_TOKEN);

        // when
        Long result = authService.refreshAccessToken(TEST_REFRESH_TOKEN);

        // then
        assertThat(result).isEqualTo(TEST_MEMBER_ID);
    }
    
    @Test
    @DisplayName("리프레시 토큰 검증 실패 - 유효하지 않은 토큰")
    void refreshAccessToken_InvalidToken_ThrowsException() {
        // given
        given(jwtTokenProvider.validateToken(TEST_REFRESH_TOKEN)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.refreshAccessToken(TEST_REFRESH_TOKEN))
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
        assertThatThrownBy(() -> authService.refreshAccessToken(TEST_REFRESH_TOKEN))
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
        assertThatThrownBy(() -> authService.refreshAccessToken(TEST_REFRESH_TOKEN))
                .isInstanceOf(AuthException.class)
                .extracting(e -> ((AuthException) e).getErrorCode())
                .isEqualTo(INVALID_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("PIN 로그인 성공")
    void loginWithPin_Success() {
        // given
        Member member = Member.builder()
                .id(TEST_MEMBER_ID)
                .email(TEST_EMAIL)
                .pin(TEST_ENCODED_PIN)
                .role(Role.USER)
                .build();
        
        given(memberRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(member));
        given(passwordEncoder.matches(TEST_PIN, TEST_ENCODED_PIN)).willReturn(true);
        given(jwtTokenProvider.createAuthenticationByMember(member)).willReturn(authentication);
        given(jwtTokenProvider.createAccessToken(authentication, TEST_MEMBER_ID)).willReturn(TEST_ACCESS_TOKEN);
        given(jwtTokenProvider.createRefreshToken(authentication, member.getId())).willReturn(TEST_REFRESH_TOKEN);
        
        // when
        Authentication result = authService.loginWithPin(TEST_EMAIL, TEST_PIN);
        
        // then
        assertThat(result).isEqualTo(authentication);
    }
    
    @Test
    @DisplayName("PIN 로그인 실패 - 회원 없음")
    void loginWithPin_MemberNotFound_ThrowsException() {
        // given
        given(memberRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.empty());
        
        // when & then
        assertThatThrownBy(() -> authService.loginWithPin(TEST_EMAIL, TEST_PIN))
            .isInstanceOf(MemberException.class)
            .extracting(e -> ((MemberException) e).getErrorCode())
            .isEqualTo(NOT_FOUND_MEMBER);
    }
    
    @Test
    @DisplayName("PIN 로그인 실패 - 잘못된 PIN")
    void loginWithPin_InvalidPin_ThrowsException() {
        // given
        Member member = Member.builder()
                .id(TEST_MEMBER_ID)
                .email(TEST_EMAIL)
                .pin(TEST_ENCODED_PIN)
                .role(Role.USER)
                .build();
        
        given(memberRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(member));
        given(passwordEncoder.matches(TEST_PIN, TEST_ENCODED_PIN)).willReturn(false);
        
        // when & then
        assertThatThrownBy(() -> authService.loginWithPin(TEST_EMAIL, TEST_PIN))
            .isInstanceOf(AuthException.class)
            .extracting(e -> ((AuthException) e).getErrorCode())
            .isEqualTo(INVALID_CREDENTIALS);
    }
    
    @Test
    @DisplayName("회원 존재 여부 확인 - 회원 존재")
    void isExistMember_MemberExists_ReturnsTrue() {
        // given
        given(memberRepository.existsByEmail(TEST_EMAIL)).willReturn(true);
        
        // when
        boolean result = authService.isExistMember(TEST_EMAIL);
        
        // then
        assertThat(result).isTrue();
    }
    
    @Test
    @DisplayName("회원 존재 여부 확인 - 회원 없음")
    void isExistMember_MemberNotExists_ReturnsFalse() {
        // given
        given(memberRepository.existsByEmail(TEST_EMAIL)).willReturn(false);
        
        // when
        boolean result = authService.isExistMember(TEST_EMAIL);
        
        // then
        assertThat(result).isFalse();
    }
    
    @Test
    @DisplayName("인증번호 검증 - AUTH_CODE 템플릿")
    void verifyAuthCodeByTemplate_WithCertificateTemplate_ReturnsEmailResult() {
        // given
        given(redisTemplate.opsForValue().get(TEST_EMAIL)).willReturn(TEST_AUTH_CODE);
        
        // when
        AuthVerificationResult result = authService.verifyAuthCodeByTemplate(TEST_EMAIL, TEST_AUTH_CODE, EmailTemplate.AUTH_CODE);
        
        // then
        assertThat(result).isNotNull();
        assertThat(result.hasAuthentication()).isFalse();
        assertThat(result.getAuthentication()).isNull();
        assertThat(result.getEmail()).isEqualTo(TEST_EMAIL);
        
        // Redis에서 인증 코드가 삭제되었는지 확인
        verify(redisTemplate).delete(TEST_EMAIL);
    }
    
    @Test
    @DisplayName("인증번호 검증 - PIN_RESET 템플릿")
    void verifyAuthCodeByTemplate_WithPinResetTemplate_ReturnsAuthenticationResult() {
        // given
        Member member = Member.builder()
                .email(TEST_EMAIL)
                .name("테스트")
                .build();
        Authentication mockAuthentication = mock(Authentication.class);
        
        given(memberRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(member));
        given(redisTemplate.opsForValue().get(TEST_EMAIL)).willReturn(TEST_AUTH_CODE);
        given(jwtTokenProvider.createAuthenticationByMember(any(Member.class))).willReturn(mockAuthentication);
        
        // when
        AuthVerificationResult result = authService.verifyAuthCodeByTemplate(TEST_EMAIL, TEST_AUTH_CODE, EmailTemplate.PIN_RESET);
        
        // then
        assertThat(result).isNotNull();
        assertThat(result.hasAuthentication()).isTrue();
        assertThat(result.getAuthentication()).isEqualTo(mockAuthentication);
        assertThat(result.getEmail()).isNull();
        
        // Redis에서 인증 코드가 삭제되었는지 확인
        verify(redisTemplate).delete(TEST_EMAIL);
    }
    
    @Test
    @DisplayName("인증번호 검증 - 유효하지 않은 템플릿")
    void verifyAuthCodeByTemplate_WithInvalidTemplate_ThrowsException() {
        // given
        EmailTemplate invalidTemplate = null;
        
        // when & then
        assertThatThrownBy(() -> 
            authService.verifyAuthCodeByTemplate(TEST_EMAIL, TEST_AUTH_CODE, invalidTemplate)
        ).isInstanceOf(EmailException.class)
         .hasFieldOrPropertyWithValue("errorCode", EmailErrorCode.INVALID_EMAIL_TEMPLATE);
    }
}
