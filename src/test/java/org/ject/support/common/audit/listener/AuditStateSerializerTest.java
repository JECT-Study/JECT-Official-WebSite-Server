package org.ject.support.common.audit.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.EntityType;
import org.hibernate.type.CollectionType;
import org.ject.support.base.UnitTestSupport;
import org.junit.jupiter.api.Test;

class AuditStateSerializerTest extends UnitTestSupport {

    @Test
    void 속성명과_상태_배열을_맵으로_변환한다() {
        // given
        String[] propertyNames = {"name", "isDeleted"};
        Object[] state = {"홍길동", Boolean.FALSE};

        // when
        Map<String, Object> result = AuditStateSerializer.toStateMap(propertyNames, state, null, null);

        // then
        assertThat(result).containsExactly(
            Map.entry("name", "홍길동"),
            Map.entry("isDeleted", false)
        );
    }

    @Test
    void 연관_엔티티_속성은_식별자로_기록된다() {
        // given
        Object parent = new Object();
        EntityType entityType = mock(EntityType.class);
        SharedSessionContractImplementor session = mock(SharedSessionContractImplementor.class);
        EntityPersister associatedPersister = mock(EntityPersister.class);
        SessionFactoryImplementor sessionFactory = mock(SessionFactoryImplementor.class);
        when(session.getFactory()).thenReturn(sessionFactory);
        when(entityType.getAssociatedEntityPersister(sessionFactory)).thenReturn(associatedPersister);
        when(associatedPersister.getIdentifier(parent, session)).thenReturn(7L);

        String[] propertyNames = {"name", "parent"};
        Object[] state = {"홍길동", parent};

        // when
        Map<String, Object> result = AuditStateSerializer.toStateMap(
            propertyNames, state, new org.hibernate.type.Type[]{null, entityType}, session);

        // then
        assertThat(result).containsExactly(
            Map.entry("name", "홍길동"),
            Map.entry("parent", 7L)
        );
    }

    @Test
    void 컬렉션_속성은_기록에서_제외된다() {
        // given
        CollectionType collectionType = mock(CollectionType.class);
        String[] propertyNames = {"name", "children"};
        Object[] state = {"홍길동", java.util.List.of(1L, 2L)};

        // when
        Map<String, Object> result = AuditStateSerializer.toStateMap(
            propertyNames, state, new org.hibernate.type.Type[]{null, collectionType}, null);

        // then
        assertThat(result).containsExactly(Map.entry("name", "홍길동"));
    }

    @Test
    void 상태_배열이_null이면_빈_맵을_반환한다() {
        // when
        Map<String, Object> result = AuditStateSerializer.toStateMap(new String[]{"name"}, null, null, null);

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
