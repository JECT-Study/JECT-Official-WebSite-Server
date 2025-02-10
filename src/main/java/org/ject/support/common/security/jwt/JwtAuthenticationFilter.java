package org.ject.support.common.security.jwt;



import static org.ject.support.common.security.SecurityErrorCode.INVALID_ACCESS_TOKEN;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ject.support.common.security.SecurityException;
import org.ject.support.domain.auth.AuthErrorCode;
import org.ject.support.domain.auth.AuthException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends GenericFilterBean {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

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
                throw new SecurityException(INVALID_ACCESS_TOKEN);
            }

            Authentication auth = jwtTokenProvider.getAuthenticationByToken(token);
            SecurityContextHolder.getContext().setAuthentication(auth);

            chain.doFilter(request, response);

        } catch (AuthException e) {
            log.error("AuthException: {}", e.getErrorCode());
            request.setAttribute("exception", e.getErrorCode().getMessage());
            sendErrorResponse(res, (AuthErrorCode) e.getErrorCode());
        }
    }

    private void sendErrorResponse(HttpServletResponse response, AuthErrorCode authErrorCode) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("utf-8");
        response.setContentType("application/json");

        LinkedHashMap<String, Object> jsonMessage = new LinkedHashMap<>();
        jsonMessage.put("status", authErrorCode.getCode());
        jsonMessage.put("data", authErrorCode.getMessage());
        jsonMessage.put("timestamp", LocalDateTime.now().toString());

        response.getWriter().write(objectMapper.writeValueAsString(jsonMessage));
    }
}

