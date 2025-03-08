package org.ject.support.common.security.jwt;

import static org.ject.support.common.exception.GlobalErrorCode.INVALID_PERMISSION;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ject.support.common.exception.GlobalException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/*
 * 인증된 사용자가 권한이 없는 리소스에 접근할 때 처리하는 클래스
 * 권한이 없는 요청에 대해 INVALID_PERMISSION 에러 응답 반환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        log.error("AccessDeniedException: {}", accessDeniedException.getMessage());
        throw new GlobalException(INVALID_PERMISSION);
    }
}
