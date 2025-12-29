package org.ject.support.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.ject.support.common.exception.GlobalErrorCode;
import org.ject.support.common.exception.GlobalException;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class String2MapSerializer {

    private final ObjectMapper objectMapper;

    public Map<String,String> serializeAsMap(final String string) {
        try {
            if (string == null || string.isBlank()) {
                return Map.of();
            }
            return objectMapper.readValue(string, new TypeReference<Map<String, String>>() {
            });
        } catch (JsonProcessingException e) {
            throw new GlobalException(GlobalErrorCode.JSON_MARSHALLING_FAILURE);
        }
    }
}
