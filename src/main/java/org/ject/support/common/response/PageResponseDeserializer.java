package org.ject.support.common.response;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.List;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@JsonComponent
public class PageResponseDeserializer extends JsonDeserializer<PageImpl<?>> {

    @Override
    public PageImpl<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        JsonNode contentNode = node.get("content");
        JsonNode pageableNode = node.get("pageable");
        JsonNode totalElementsNode = node.get("totalElements");

        List<?> content = p.getCodec().treeToValue(contentNode, List.class);

        int page = pageableNode.get("pageNumber").asInt();
        int size = pageableNode.get("pageSize").asInt();
        long total = totalElementsNode.asLong();

        return new PageImpl<>(content, PageRequest.of(page, size), total);
    }
}
