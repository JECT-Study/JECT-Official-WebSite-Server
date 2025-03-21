package org.ject.support.common.security.jwt;

import static org.ject.support.common.exception.GlobalErrorCode.INTERNAL_SERVER_ERROR;
import static org.ject.support.common.exception.GlobalErrorCode.INVALID_ACCESS_TOKEN;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ject.support.common.exception.GlobalErrorCode;
import org.ject.support.common.exception.GlobalException;
import org.ject.support.common.response.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtExceptionHandlerFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws IOException {
        try {
            filterChain.doFilter(request, response);
            log.info("Request URL: {}, Response Status: {}", request.getRequestURI(), response.getStatus());
        } catch (GlobalException e) {
            setErrorResponse(response, e.getErrorCode());
            log.error("GlobalException: {}", e.getErrorCode().getMessage());
        } catch (AuthenticationException e) {
            setErrorResponse(response, INVALID_ACCESS_TOKEN);
            log.error("AuthenticationException: {}", e.getMessage());
        } catch (ServletException e) {
            if (e.getCause() instanceof ClassCastException) {
                setErrorResponse(response, INVALID_ACCESS_TOKEN);
                log.error("ClassCastException: {}", e.getCause().getMessage());
            } else {
                setErrorResponse(response, INTERNAL_SERVER_ERROR);
                log.error("Exception: {}", e.getMessage());
            }
        }
    }

    private void setErrorResponse(
            HttpServletResponse response,
            GlobalErrorCode errorCode
    ) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        ApiResponse<String> errorResponse = new ApiResponse<>(
                errorCode.getCode(),
                errorCode.getMessage()
        );

        String responseMessage = objectMapper.writeValueAsString(errorResponse);
        StreamUtils.copy(responseMessage.getBytes(StandardCharsets.UTF_8), response.getOutputStream());
    }
}
