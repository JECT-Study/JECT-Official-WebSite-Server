package org.ject.support.common.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.ject.support.common.security.CustomUserDetails;
import org.ject.support.common.security.CustomUserDetailService;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Member;
import org.ject.support.domain.member.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private CustomUserDetailService customUserDetailService;

    @Mock
    private HttpServletRequest request;

    private Member testMember;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(customUserDetailService);
        ReflectionTestUtils.setField(jwtTokenProvider, "accessExpirationTime", 3600000L); // 1시간
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshExpirationTime", 1209600000L); // 2주

        testMember = Member.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .phoneNumber("01012345678")
                .role(Role.USER)
                .jobFamily(JobFamily.BE)
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(testMember);
        authentication = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    @Test
    @DisplayName("Access 토큰 생성 테스트")
    void createAccessToken() {
        // when
        String token = jwtTokenProvider.createAccessToken(authentication, testMember.getId());

        // then
        assertThat(token).isNotNull();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getMemberId(token)).isEqualTo(testMember.getId());
    }

    @Test
    @DisplayName("Refresh 토큰 생성 테스트")
    void createRefreshToken() {
        // when
        String token = jwtTokenProvider.createRefreshToken(authentication);

        // then
        assertThat(token).isNotNull();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("토큰으로부터 Authentication 객체 추출 테스트")
    void getAuthenticationByToken() {
        // given
        String token = jwtTokenProvider.createAccessToken(authentication, testMember.getId());
        when(customUserDetailService.loadUserByUsername(anyString()))
                .thenReturn(new CustomUserDetails(testMember));

        // when
        Authentication resultAuth = jwtTokenProvider.getAuthenticationByToken(token);

        // then
        assertThat(resultAuth).isNotNull();
        assertThat(resultAuth.getName()).isEqualTo(testMember.getEmail());
    }

    @Test
    @DisplayName("HTTP 요청에서 토큰 추출 테스트")
    void resolveToken() {
        // given
        String token = "test-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        // when
        String resolvedToken = jwtTokenProvider.resolveToken(request);

        // then
        assertThat(resolvedToken).isEqualTo(token);
    }

    @Test
    @DisplayName("쿠키 생성 테스트")
    void createCookies() {
        // given
        String token = "test-token";

        // when
        Cookie refreshCookie = JwtTokenProvider.createRefreshCookie(token);
        Cookie accessCookie = JwtTokenProvider.createAccessCookie(token);

        // then
        assertThat(refreshCookie.getName()).isEqualTo("refreshToken");
        assertThat(refreshCookie.getValue()).isEqualTo(token);
        assertThat(refreshCookie.isHttpOnly()).isTrue();

        assertThat(accessCookie.getName()).isEqualTo("accessToken");
        assertThat(accessCookie.getValue()).isEqualTo(token);
        assertThat(accessCookie.isHttpOnly()).isTrue();
    }

    @Test
    @DisplayName("토큰 재발급 테스트")
    void reissueAccessToken() {
        // given
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);
        when(customUserDetailService.loadUserByUsername(anyString()))
                .thenReturn(new CustomUserDetails(testMember));

        // when
        String newAccessToken = jwtTokenProvider.reissueAccessToken(refreshToken, testMember.getId());

        // then
        assertThat(newAccessToken).isNotNull();
        assertThat(jwtTokenProvider.validateToken(newAccessToken)).isTrue();
        assertThat(jwtTokenProvider.getMemberId(newAccessToken)).isEqualTo(testMember.getId());
    }

    @Test
    @DisplayName("유효하지 않은 토큰 검증 테스트")
    void validateInvalidToken() {
        // given
        String invalidToken = "invalid-token";

        // when & then
        assertThat(jwtTokenProvider.validateToken(invalidToken)).isFalse();
    }

    @Test
    @DisplayName("Authentication이 null일 때 예외 발생 테스트")
    void validateNullAuthentication() {
        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.createAccessToken(null, testMember.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Authentication is required");
    }
}
