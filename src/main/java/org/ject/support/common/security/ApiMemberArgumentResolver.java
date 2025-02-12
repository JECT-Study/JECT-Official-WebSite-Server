package org.ject.support.common.security;


import static org.ject.support.common.exception.GlobalErrorCode.EMPTY_ACCESS_TOKEN;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.ject.support.common.exception.GlobalException;
import org.ject.support.common.security.jwt.JwtTokenProvider;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class ApiMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(ApiMember.class) && parameter.hasParameterAnnotation(
                AuthPrincipal.class);
    }

    @Override
    public ApiMember resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                     NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        String accessToken = jwtTokenProvider.resolveToken(request);

        if (accessToken == null) {
            throw new GlobalException(EMPTY_ACCESS_TOKEN);
        }

        String token = jwtTokenProvider.resolveToken(request);
        jwtTokenProvider.validateToken(token);
        Long memberId = jwtTokenProvider.getMemberId(token);
        
        return new ApiMember(memberId);
    }
}
