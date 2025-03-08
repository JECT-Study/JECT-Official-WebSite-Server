package org.ject.support.common.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class CommonHttpMessageConverter extends AbstractHttpMessageConverter<ApiResponse<Object>> {
    private final ObjectMapper objectMapper;

    @Override
    protected boolean supports(final Class<?> clazz) {
        return clazz.equals(ApiResponse.class) || clazz.isPrimitive() || clazz.equals(String.class);
    }

    /**
     *
     */
    @Override
    protected ApiResponse<Object> readInternal(final Class<? extends ApiResponse<Object>> clazz,
                                               final HttpInputMessage inputMessage)
            throws HttpMessageNotReadableException {
        throw new UnsupportedOperationException("this converter does not support reading");
    }

    @Override
    protected void writeInternal(final ApiResponse<Object> objectApiResponse, final HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        String responseMessage = objectMapper.writeValueAsString(outputMessage);
        StreamUtils.copy(responseMessage.getBytes(StandardCharsets.UTF_8), outputMessage.getBody());
    }

    @Override
    protected void addDefaultHeaders(HttpHeaders headers, ApiResponse<Object> objectApiResponse, MediaType contentType)
    {
        try {
            super.addDefaultHeaders(headers, objectApiResponse, contentType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
