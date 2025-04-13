package org.ject.support.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StringListConverterTest {

    StringListConverter stringListConverter = new StringListConverter();

    @Test
    @DisplayName("convert into List cannot resolve blank string")
    void convert_into_list_cannot_resolve_blank_string() {
        // given & when
        List<String> strings = stringListConverter.convertToEntityAttribute("");
        // then
        assertThat(strings).isEmpty();
    }

    @Test
    @DisplayName("convert into List cannot resolve null string")
    void convert_into_list_cannot_resolve_null_string() {
        // given & when
        List<String> strings = stringListConverter.convertToEntityAttribute(null);
        // then
        assertThat(strings).isEmpty();
    }

}
