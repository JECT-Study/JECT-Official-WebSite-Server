package org.ject.support.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.ject.support.common.exception.GlobalErrorCode;
import org.ject.support.common.exception.GlobalException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Json2MapSerializer {
    private final ObjectMapper objectMapper;

    public Map<String,String> serializeAsMap(final Object object) {
        try {
            String json = objectMapper.writeValueAsString(object);
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new GlobalException(GlobalErrorCode.JSON_MARSHALLING_FAILURE);
        }
    }

    public String serializeAsString(final Map<String, String> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new GlobalException(GlobalErrorCode.JSON_MARSHALLING_FAILURE);
        }
    }
}
