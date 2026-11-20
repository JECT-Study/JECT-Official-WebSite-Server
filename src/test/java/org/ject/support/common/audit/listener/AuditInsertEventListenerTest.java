package org.ject.support.common.audit.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.persister.entity.EntityPersister;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.common.audit.AuditContext;
import org.ject.support.common.audit.AuditContextHolder;
import org.ject.support.common.audit.listener.AuditListenerTestFixtures.AuditableEntity;
import org.ject.support.common.audit.listener.AuditListenerTestFixtures.NonAuditableEntity;
import org.ject.support.domain.changehistory.domain.ChangeActionType;
import org.ject.support.domain.changehistory.domain.ChangeDetail;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class AuditInsertEventListenerTest extends UnitTestSupport {

    private final AuditInsertEventListener listener = new AuditInsertEventListener();

    @AfterEach
    void tearDown() {
        AuditContextHolder.clear();
    }

    @Test
    void 컨텍스트가_없으면_수집하지_않는다() {
        // given
        PostInsertEvent event = insertEvent(new AuditableEntity(), new String[]{"name"}, new Object[]{"홍길동"});

        // when
        listener.onPostInsert(event);

        // then
        assertThat(AuditContextHolder.get()).isNull();
    }

    @Test
    void Auditable가_아니면_수집하지_않는다() {
        // given
        AuditContext context = new AuditContext("DOMAIN", ChangeActionType.CREATE, "1", null);
        AuditContextHolder.set(context);
        PostInsertEvent event = insertEvent(new NonAuditableEntity(), new String[]{"name"}, new Object[]{"홍길동"});

        // when
        listener.onPostInsert(event);

        // then
        assertThat(context.getDetails()).isEmpty();
    }

    @Test
    void CREATE_상세가_컨텍스트에_수집된다() {
        // given
        AuditContext context = new AuditContext("DOMAIN", ChangeActionType.CREATE, "1", null);
        AuditContextHolder.set(context);
        PostInsertEvent event = insertEvent(new AuditableEntity(), new String[]{"name"}, new Object[]{"홍길동"});

        // when
        listener.onPostInsert(event);

        // then
        assertThat(context.getDetails()).hasSize(1);
        ChangeDetail detail = context.getDetails().get(0);
        assertThat(detail.getEntityName()).isEqualTo(AuditableEntity.class.getSimpleName());
        assertThat(detail.getEntityId()).isEqualTo("1");
        assertThat(detail.getActionType()).isEqualTo(ChangeActionType.CREATE);
        assertThat(detail.getBeforeData()).isNull();
        assertThat(detail.getAfterData()).contains("홍길동");
    }

    @Test
    void 커밋_이후_처리가_필요하지_않다() {
        assertThat(listener.requiresPostCommitHandling(mock(EntityPersister.class))).isFalse();
    }

    private PostInsertEvent insertEvent(Object entity, String[] propertyNames, Object[] state) {
        EntityPersister persister = mock(EntityPersister.class);
        lenient().when(persister.getPropertyNames()).thenReturn(propertyNames);
        return new PostInsertEvent(entity, 1L, state, persister, mock(EventSource.class));
    }
}
