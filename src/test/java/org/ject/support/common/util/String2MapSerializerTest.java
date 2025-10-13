package org.ject.support.common.util;

import org.assertj.core.api.Assertions;
import org.ject.support.common.exception.GlobalException;
import org.ject.support.testconfig.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@IntegrationTest
class String2MapSerializerTest {

    @Autowired
    String2MapSerializer string2MapSerializer;

    @Test
    void 임의의_문자열을_Map으로_직렬화_성공() {
        // given
        String string = """
                {
                    "key1":"value1",
                    "key2":"value2",
                    "key3":"value3"
                }
                """;

        Map<String, String> map = Map.of(
                "key1", "value1",
                "key2", "value2",
                "key3", "value3"
        );

        // when
        Map<String, String> result = string2MapSerializer.serializeAsMap(string);

        // then
        assertThat(result).isEqualTo(map);
    }

    @Test
    void 문자열이_공백이거나_null이면_빈_Map_반환() {
        // given
        String blankString = "   ";
        String emptyString = "";
        String nullString = null;

        // when
        Map<String, String> resultOfBlank = string2MapSerializer.serializeAsMap(blankString);
        Map<String, String> resultOfEmpty = string2MapSerializer.serializeAsMap(emptyString);
        Map<String, String> resultOfNull = string2MapSerializer.serializeAsMap(nullString);

        // then
        assertThat(resultOfBlank).isEmpty();
        assertThat(resultOfEmpty).isEmpty();
        assertThat(resultOfNull).isEmpty();
    }

    @Test
    void 문자열_형태가_JSON이_아니면_실패() {
        // when, then
        Assertions.assertThatThrownBy(() -> string2MapSerializer.serializeAsMap("I'm not json"))
                .isInstanceOf(GlobalException.class);
    }
}