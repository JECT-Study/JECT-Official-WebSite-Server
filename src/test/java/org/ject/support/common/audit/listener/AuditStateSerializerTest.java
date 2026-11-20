package org.ject.support.common.audit.listener;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.ject.support.base.UnitTestSupport;
import org.junit.jupiter.api.Test;

class AuditStateSerializerTest extends UnitTestSupport {

    @Test
    void 속성명과_상태_배열을_맵으로_변환한다() {
        // given
        String[] propertyNames = {"name", "isDeleted"};
        Object[] state = {"홍길동", Boolean.FALSE};

        // when
        Map<String, Object> result = AuditStateSerializer.toStateMap(propertyNames, state);

        // then
        assertThat(result).containsExactly(
            Map.entry("name", "홍길동"),
            Map.entry("isDeleted", false)
        );
    }

    @Test
    void 상태_배열이_null이면_빈_맵을_반환한다() {
        // when
        Map<String, Object> result = AuditStateSerializer.toStateMap(new String[]{"name"}, null);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 속성명의_인덱스를_찾는다() {
        // given
        String[] propertyNames = {"name", "isDeleted"};

        // when & then
        assertThat(AuditStateSerializer.indexOf(propertyNames, "isDeleted")).isEqualTo(1);
        assertThat(AuditStateSerializer.indexOf(propertyNames, "unknown")).isEqualTo(-1);
    }

    @Test
    void 맵을_JSON_문자열로_직렬화한다() {
        // given
        Map<String, Object> state = Map.of("name", "홍길동");

        // when
        String json = AuditStateSerializer.writeJson(state);

        // then
        assertThat(json).contains("\"name\"").contains("홍길동");
    }
}
