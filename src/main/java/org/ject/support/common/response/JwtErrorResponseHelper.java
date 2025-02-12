package org.ject.support.common.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.ject.support.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtErrorResponseHelper {
    
    private final ObjectMapper objectMapper;

    public void sendErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        setupResponse(response);
        
        Map<String, Object> jsonMessage = new LinkedHashMap<>();
        jsonMessage.put("status", errorCode.getCode());
        jsonMessage.put("data", errorCode.getMessage());
        jsonMessage.put("timestamp", LocalDateTime.now().toString());

        writeResponse(response, jsonMessage);
    }

    private void setupResponse(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("utf-8");
        response.setContentType("application/json");
    }

    private void writeResponse(HttpServletResponse response, Map<String, Object> jsonMessage) throws IOException {
        response.getWriter().write(objectMapper.writeValueAsString(jsonMessage));
    }
}
