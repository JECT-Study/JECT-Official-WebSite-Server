package org.ject.support.common.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.ject.support.common.exception.GlobalErrorCode.AUTHENTICATION_REQUIRED;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.common.exception.GlobalException;
import org.ject.support.common.security.CustomUserDetails;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.entity.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

class JwtTokenProviderTest extends UnitTestSupport {

    private JwtTokenProvider jwtTokenProvider;

    private JwtCookieProvider jwtCookieProvider;

    @Mock
    private HttpServletRequest request;

    private Member testMember;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        jwtCookieProvider = new JwtCookieProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "accessExpirationTime", 3600000L); // 1시간
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshExpirationTime", 1209600000L); // 2주
        
        // secretKey 초기화 추가
        String salt = "secretkeysecretkeysecretkeysecretkeysecretkeysecretkeysecretkeysecretkeysecretkeysecretkey";
        ReflectionTestUtils.setField(jwtTokenProvider, "salt", salt);
        jwtTokenProvider.init(); // PostConstruct 메서드 직접 호출

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
    void Access_토큰_생성_테스트() {
        // when
        String token = jwtTokenProvider.createAccessToken(authentication, testMember.getId());

        // then
        assertThat(token).isNotNull();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getMemberId(token)).isEqualTo(testMember.getId());
    }

    @Test
    void Refresh_토큰_생성_테스트() {
        // when
        String token = jwtTokenProvider.createRefreshToken(authentication, testMember.getId());

        // then
        assertThat(token).isNotNull();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    void 토큰으로부터_Authentication_객체_추출_테스트() {
        // given
        String token = jwtTokenProvider.createAccessToken(authentication, testMember.getId());

        // when
        Authentication resultAuth = jwtTokenProvider.getAuthenticationByToken(token);

        // then
        assertThat(resultAuth).isNotNull();
        assertThat(resultAuth.getName()).isEqualTo(testMember.getEmail());
        assertThat(((CustomUserDetails) resultAuth.getPrincipal()).getMemberId()).isEqualTo(testMember.getId());
    }

    @Test
    void HTTP_요청에서_토큰_추출_테스트() {
        // given
        String token = "test-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        // when
        String resolvedToken = jwtTokenProvider.resolveAccessToken(request);

        // then
        assertThat(resolvedToken).isEqualTo(token);
    }

    @Test
    void 쿠키_생성_테스트() {
        // given
        String token = "test-token";

        // when
        Cookie refreshCookie = jwtCookieProvider.createRefreshCookie(token);
        Cookie accessCookie = jwtCookieProvider.createAccessCookie(token);

        // then
        assertThat(refreshCookie.getName()).isEqualTo("refreshToken");
        assertThat(refreshCookie.getValue()).isEqualTo(token);
        assertThat(refreshCookie.isHttpOnly()).isTrue();

        assertThat(accessCookie.getName()).isEqualTo("accessToken");
        assertThat(accessCookie.getValue()).isEqualTo(token);
        assertThat(accessCookie.isHttpOnly()).isTrue();
    }

    @Test
    void 토큰_재발급_테스트() {
        // given
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication, testMember.getId());

        // when
        String newAccessToken = jwtTokenProvider.reissueAccessToken(refreshToken, testMember.getId());

        // then
        assertThat(newAccessToken).isNotNull();
        assertThat(jwtTokenProvider.validateToken(newAccessToken)).isTrue();
        assertThat(jwtTokenProvider.getMemberId(newAccessToken)).isEqualTo(testMember.getId());
        Authentication resultAuth = jwtTokenProvider.getAuthenticationByToken(newAccessToken);
        assertThat(resultAuth.getName()).isEqualTo(testMember.getEmail());
    }

    @Test
    void 유효하지_않은_토큰_검증_테스트() {
        // given
        String invalidToken = "invalid-token";

        // when & then
        assertThat(jwtTokenProvider.validateToken(invalidToken)).isFalse();
    }

    @Test
    void Authentication이_null일_때_예외_발생_테스트() {
        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.createAccessToken(null, testMember.getId()))
                .isInstanceOf(GlobalException.class)
                .extracting(e -> ((GlobalException) e).getErrorCode().getMessage())
                .isEqualTo(AUTHENTICATION_REQUIRED.getMessage());
    }

    @Test
    void 토큰에_ROLE_접두사가_포함된_역할이_저장되는지_확인() {
        // when
        String token = jwtTokenProvider.createAccessToken(authentication, testMember.getId());

        // then
        Authentication resultAuth = jwtTokenProvider.getAuthenticationByToken(token);
        assertThat(resultAuth.getAuthorities()).isNotEmpty();
        assertThat(resultAuth.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");
    }

    @Test
    void 토큰에서_ROLE_접두사가_있는_역할을_추출할_때_정상적으로_처리되는지_확인() {
        // given
        // 테스트를 위해 ROLE_ 접두사가 있는 역할을 가진 사용자로 테스트 멤버 재설정
        testMember = Member.builder()
                .id(2L)
                .email("admin@example.com")
                .name("Admin User")
                .phoneNumber("01098765432")
                .role(Role.ADMIN)
                .build();
        
        CustomUserDetails userDetails = new CustomUserDetails(testMember);
        authentication = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
        
        // when
        String token = jwtTokenProvider.createAccessToken(authentication, testMember.getId());
        Authentication resultAuth = jwtTokenProvider.getAuthenticationByToken(token);
        
        // then
        assertThat(resultAuth).isNotNull();
        assertThat(resultAuth.getName()).isEqualTo(testMember.getEmail());
        assertThat(((CustomUserDetails) resultAuth.getPrincipal()).getMemberId()).isEqualTo(testMember.getId());
        assertThat(resultAuth.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
    }
}
