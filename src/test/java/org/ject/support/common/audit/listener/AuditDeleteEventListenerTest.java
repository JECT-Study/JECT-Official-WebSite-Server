package org.ject.support.common.audit.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.PreDeleteEvent;
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

class AuditDeleteEventListenerTest extends UnitTestSupport {

    private final AuditDeleteEventListener listener = new AuditDeleteEventListener();

    @AfterEach
    void tearDown() {
        AuditContextHolder.clear();
    }

    @Test
    void 컨텍스트가_없으면_수집하지_않고_삭제를_거부하지_않는다() {
        // given
        PreDeleteEvent event = deleteEvent(new AuditableEntity(), new String[]{"name"}, new Object[]{"홍길동"});

        // when
        boolean veto = listener.onPreDelete(event);

        // then
        assertThat(veto).isFalse();
        assertThat(AuditContextHolder.get()).isNull();
    }

    @Test
    void Auditable가_아니면_수집하지_않는다() {
        // given
        AuditContext context = new AuditContext("DOMAIN", ChangeActionType.DELETE, "1", null);
        AuditContextHolder.set(context);
        PreDeleteEvent event = deleteEvent(new NonAuditableEntity(), new String[]{"name"}, new Object[]{"홍길동"});

        // when
        boolean veto = listener.onPreDelete(event);

        // then
        assertThat(veto).isFalse();
        assertThat(context.getDetails()).isEmpty();
    }

    @Test
    void DELETE_상세가_삭제_전_상태로_수집되고_삭제를_거부하지_않는다() {
        // given
        AuditContext context = new AuditContext("DOMAIN", ChangeActionType.DELETE, "1", null);
        AuditContextHolder.set(context);
        PreDeleteEvent event = deleteEvent(new AuditableEntity(), new String[]{"name"}, new Object[]{"홍길동"});

        // when
        boolean veto = listener.onPreDelete(event);

        // then
        assertThat(veto).isFalse();
        assertThat(context.getDetails()).hasSize(1);
        ChangeDetail detail = context.getDetails().get(0);
        assertThat(detail.getEntityName()).isEqualTo(AuditableEntity.class.getSimpleName());
        assertThat(detail.getEntityId()).isEqualTo("1");
        assertThat(detail.getActionType()).isEqualTo(ChangeActionType.DELETE);
        assertThat(detail.getBeforeData()).contains("홍길동");
        assertThat(detail.getAfterData()).isNull();
    }

    private PreDeleteEvent deleteEvent(Object entity, String[] propertyNames, Object[] deletedState) {
        EntityPersister persister = mock(EntityPersister.class);
        lenient().when(persister.getPropertyNames()).thenReturn(propertyNames);
        return new PreDeleteEvent(entity, 1L, deletedState, persister, mock(EventSource.class));
    }
}
