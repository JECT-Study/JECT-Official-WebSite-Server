package org.ject.support.common.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true, value = {"pageable", "sort", "empty", "first"})
public class PageResponse<E> extends PageImpl<E> {

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    private PageResponse(@JsonProperty("content") List<E> content,
                         @JsonProperty("number") int page,
                         @JsonProperty("size") int size,
                         @JsonProperty("totalElements") long total) {
        super(content, PageRequest.of(page, size), total);
    }

    public static <E> PageResponse<E> from(final List<E> content, Pageable pageable, long total) {
        return new PageResponse<>(content, pageable.getPageNumber(), pageable.getPageSize(), total);
    }

    @JsonProperty("hasNext")
    public boolean hasNext() {
        return super.hasNext();
    }
}
