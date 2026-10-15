package org.ject.support.common.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.ject.support.base.UnitTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
        ReflectionTestUtils.setField(jwtCookieProvider, "domain", "localhost");
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
}
