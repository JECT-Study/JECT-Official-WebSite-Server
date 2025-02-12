package org.ject.support.common.security.jwt;

import static org.ject.support.common.exception.GlobalErrorCode.INVALID_ACCESS_TOKEN;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ject.support.common.exception.GlobalException;
import org.ject.support.common.response.JwtErrorResponseHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

/**
 * JWT 토큰 기반의 인증을 처리하는 필터
 * 
 * 요청 헤더에서 JWT 토큰을 추출
 * 토큰의 유효성 검증
 * 유효한 토큰이면 인증 정보를 SecurityContext에 저장
 */

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends GenericFilterBean {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtErrorResponseHelper jwtErrorResponseHelper;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {

        HttpServletResponse res = (HttpServletResponse) response;

        try {

            String token = jwtTokenProvider.resolveToken((HttpServletRequest) request);

            if (token == null) {
                chain.doFilter(request, response);
                return;
            }

            if (!jwtTokenProvider.validateToken(token)) {
                throw new GlobalException(INVALID_ACCESS_TOKEN);
            }

            Authentication auth = jwtTokenProvider.getAuthenticationByToken(token);
            SecurityContextHolder.getContext().setAuthentication(auth);

            chain.doFilter(request, response);
        }
        catch (GlobalException e) {
            log.error("GlobalException: {}", e.getErrorCode());
            request.setAttribute("exception", e.getErrorCode().getMessage());
            jwtErrorResponseHelper.sendErrorResponse(res, e.getErrorCode());
        }
    }
}

