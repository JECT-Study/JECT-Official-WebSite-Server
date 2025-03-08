package org.ject.support.common.security.jwt;

import static org.ject.support.common.exception.GlobalErrorCode.INVALID_ACCESS_TOKEN;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ject.support.common.exception.GlobalException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/*
 * 인증되지 않은 사용자의 접근을 처리하는 클래스
 * 인증되지 않은 요청에 대해 401 Unauthorized 응답 반환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) {

        throw new GlobalException(INVALID_ACCESS_TOKEN);
    }
}