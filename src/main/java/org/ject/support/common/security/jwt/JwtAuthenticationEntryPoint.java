package org.ject.support.common.security.jwt;

import static org.ject.support.common.exception.GlobalErrorCode.INVALID_ACCESS_TOKEN;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ject.support.common.exception.GlobalException;
import org.springframework.http.HttpStatus;
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
                         AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("utf-8");
        response.setContentType("application/json");

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> jsonMessage = new HashMap<>();

        String errorMessage = (String) request.getAttribute("exception");
        jsonMessage.put("httpStatus", HttpStatus.UNAUTHORIZED);
        jsonMessage.put("message", errorMessage);
        String result = objectMapper.writeValueAsString(jsonMessage);

        response.getWriter().write(result);
    }
}