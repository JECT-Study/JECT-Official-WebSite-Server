package org.ject.support.common.security;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.ject.support.common.exception.GlobalException;
import org.ject.support.domain.member.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

@ExtendWith(MockitoExtension.class)
class AuthenticatedMemberIdResolverTest {

    @InjectMocks
    private AuthenticatedMemberIdResolver resolver;

    @Mock
    private MethodParameter methodParameter;

    @Mock
    private ModelAndViewContainer mavContainer;

    @Mock
    private NativeWebRequest webRequest;

    @Mock
    private WebDataBinderFactory binderFactory;

    @Mock
    private SecurityContext securityContext;

    private static final Long MEMBER_ID = 1L;
    private static final String EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("@AuthPrincipal이 붙은 Long 타입 파라미터를 지원한다")
    void supportsParameter() {
        // given
        when(methodParameter.getParameterType()).thenReturn((Class) Long.class);
        when(methodParameter.hasParameterAnnotation(AuthPrincipal.class)).thenReturn(true);

        // when
        boolean result = resolver.supportsParameter(methodParameter);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("@AuthPrincipal이 없는 Long 타입 파라미터는 지원하지 않는다")
    void notSupportsParameterWithoutAnnotation() {
        // given
        when(methodParameter.getParameterType()).thenReturn((Class) Long.class);
        when(methodParameter.hasParameterAnnotation(AuthPrincipal.class)).thenReturn(false);

        // when
        boolean result = resolver.supportsParameter(methodParameter);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("인증된 사용자의 memberId를 반환한다")
    void resolveArgumentWithAuthenticatedUser() {
        // given
        CustomUserDetails userDetails = new CustomUserDetails(EMAIL, MEMBER_ID, Role.SEMESTER);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // when
        Long result = resolver.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);

        // then
        assertThat(result).isEqualTo(MEMBER_ID);
    }

    @Test
    @DisplayName("인증되지 않은 경우 예외가 발생한다")
    void resolveArgumentWithUnauthenticatedUser() {
        // given
        when(securityContext.getAuthentication()).thenReturn(null);

        // when & then
        assertThatThrownBy(() ->
            resolver.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory)
        ).isInstanceOf(GlobalException.class);
    }

    @Test
    @DisplayName("인증은 되었지만 인증 정보가 올바르지 않은 경우 예외가 발생한다")
    void resolveArgumentWithInvalidAuthentication() {
        // given
        Authentication authentication = new UsernamePasswordAuthenticationToken("invalid", "", null);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // when & then
        assertThatThrownBy(() ->
            resolver.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory)
        ).isInstanceOf(ClassCastException.class);
    }
}
