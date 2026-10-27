package org.ject.support.common.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.ject.support.base.UnitTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

class JwtCookieProviderTest extends UnitTestSupport {

    private JwtCookieProvider jwtCookieProvider;

    @BeforeEach
    void setUp() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("local");
        jwtCookieProvider = new JwtCookieProvider(environment);
        ReflectionTestUtils.setField(jwtCookieProvider, "refreshExpirationTime", 259200000L);
    }

    @Test
    @DisplayName("로그인 쿠키를 삭제하면 액세스 토큰과 리프레시 토큰만 만료한다")
    void 로그인_쿠키를_삭제하면_액세스_토큰과_리프레시_토큰만_만료한다() {
        // given
        var response = new MockHttpServletResponse();

        // when
        jwtCookieProvider.deleteLoginCookies(response);

        // then
        List<String> cookies = response.getHeaders("Set-Cookie");
        assertThat(cookies).hasSize(2);
        assertThat(cookies).anyMatch(cookie -> cookie.startsWith("accessToken=") && cookie.contains("Max-Age=0"));
        assertThat(cookies).anyMatch(cookie -> cookie.startsWith("refreshToken=") && cookie.contains("Max-Age=0"));
    }

    @Test
    @DisplayName("로그인 쿠키 삭제 시 인증 토큰은 삭제하지 않는다")
    void 로그인_쿠키_삭제시_인증_토큰은_삭제하지_않는다() {
        // given
        var response = new MockHttpServletResponse();

        // when
        jwtCookieProvider.deleteLoginCookies(response);

        // then
        assertThat(response.getHeaders("Set-Cookie"))
                .noneMatch(cookie -> cookie.startsWith("verificationToken="));
    }

    @ParameterizedTest
    @ValueSource(strings = {"local", "dev", "prod"})
    void 인증_쿠키는_발급한_호스트에서만_사용한다(String profile) {
        // given
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles(profile);
        JwtCookieProvider provider = new JwtCookieProvider(environment);
        ReflectionTestUtils.setField(provider, "refreshExpirationTime", 259200000L);

        // when
        var cookies = List.of(provider.createAccessCookie("access"), provider.createRefreshCookie("refresh"),
                provider.createVerificationCookie("verification"));

        // then
        assertThat(cookies).allSatisfy(cookie -> {
            assertThat(cookie.getDomain()).isNull();
            assertThat(cookie.toString()).doesNotContain("Domain=");
            assertThat(cookie.getPath()).isEqualTo("/");
            assertThat(cookie.isHttpOnly()).isTrue();
            assertThat(cookie.isSecure()).isTrue();
        });
    }

    @Test
    void 인증_쿠키_삭제도_Domain_없이_같은_호스트에서_만료한다() {
        // given
        var response = new MockHttpServletResponse();

        // when
        jwtCookieProvider.deleteAuthCookies(response);

        // then
        assertThat(response.getHeaders("Set-Cookie")).hasSize(3)
                .allSatisfy(cookie -> assertThat(cookie).contains("Max-Age=0", "Path=/").doesNotContain("Domain="));
    }

}
