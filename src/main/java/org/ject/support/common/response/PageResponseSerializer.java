package org.ject.support.common.response;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import org.ject.support.common.data.PageResponse;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.data.domain.Page;

@JsonComponent
public class PageResponseSerializer<T> extends JsonSerializer<Page<T>> {

    @Override
    public void serialize(Page<T> page, JsonGenerator gen, SerializerProvider serializers) throws IOException {
//        if (page.isEmpty()) {
//            throw new GlobalException(GlobalErrorCode.RESOURCE_NOT_FOUND);
//        }
        PageResponse<T> response = PageResponse.from(page);
        gen.writeObject(response);
    }
}
