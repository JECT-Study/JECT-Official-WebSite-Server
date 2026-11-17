package org.ject.support.common.audit.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.PreUpdateEvent;
import org.hibernate.persister.entity.EntityPersister;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.common.audit.AuditContext;
import org.ject.support.common.audit.AuditContextHolder;
import org.ject.support.common.audit.listener.AuditListenerTestFixtures.AuditableEntity;
import org.ject.support.common.audit.listener.AuditListenerTestFixtures.AuditableSoftDeletableEntity;
import org.ject.support.common.audit.listener.AuditListenerTestFixtures.NonAuditableEntity;
import org.ject.support.domain.changehistory.domain.ChangeActionType;
import org.ject.support.domain.changehistory.domain.ChangeDetail;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class AuditUpdateEventListenerTest extends UnitTestSupport {

    private static final String[] PROPERTY_NAMES = {"name", "isDeleted"};

    private final AuditUpdateEventListener listener = new AuditUpdateEventListener();

    @AfterEach
    void tearDown() {
        AuditContextHolder.clear();
    }

    @Test
    void 컨텍스트가_없으면_수집하지_않고_수정을_거부하지_않는다() {
        // given
        PreUpdateEvent event = updateEvent(new AuditableEntity(),
            new Object[]{"변경전", false}, new Object[]{"변경후", false});

        // when
        boolean veto = listener.onPreUpdate(event);

        // then
        assertThat(veto).isFalse();
        assertThat(AuditContextHolder.get()).isNull();
    }

    @Test
    void Auditable가_아니면_수집하지_않는다() {
        // given
        AuditContext context = new AuditContext("DOMAIN", ChangeActionType.UPDATE, "1", null);
        AuditContextHolder.set(context);
        PreUpdateEvent event = updateEvent(new NonAuditableEntity(),
            new Object[]{"변경전", false}, new Object[]{"변경후", false});

        // when
        listener.onPreUpdate(event);

        // then
        assertThat(context.getDetails()).isEmpty();
    }

    @Test
    void 변경된_속성이_없으면_수집하지_않는다() {
        // given
        AuditContext context = new AuditContext("DOMAIN", ChangeActionType.UPDATE, "1", null);
        AuditContextHolder.set(context);
        PreUpdateEvent event = updateEvent(new AuditableEntity(),
            new Object[]{"동일값", false}, new Object[]{"동일값", false});

        // when
        listener.onPreUpdate(event);

        // then
        assertThat(context.getDetails()).isEmpty();
    }

    @Test
    void 변경된_속성만_UPDATE_상세로_수집된다() {
        // given
        AuditContext context = new AuditContext("DOMAIN", ChangeActionType.UPDATE, "1", null);
        AuditContextHolder.set(context);
        PreUpdateEvent event = updateEvent(new AuditableEntity(),
            new Object[]{"변경전", false}, new Object[]{"변경후", false});

        // when
        boolean veto = listener.onPreUpdate(event);

        // then
        assertThat(veto).isFalse();
        assertThat(context.getDetails()).hasSize(1);
        ChangeDetail detail = context.getDetails().get(0);
        assertThat(detail.getActionType()).isEqualTo(ChangeActionType.UPDATE);
        assertThat(detail.getBeforeData()).contains("변경전").doesNotContain("isDeleted");
        assertThat(detail.getAfterData()).contains("변경후").doesNotContain("isDeleted");
    }

    @Test
    void SoftDeletable_엔티티의_isDeleted가_true로_바뀌면_DELETE로_분류된다() {
        // given
        AuditContext context = new AuditContext("DOMAIN", ChangeActionType.UPDATE, "1", null);
        AuditContextHolder.set(context);
        PreUpdateEvent event = updateEvent(new AuditableSoftDeletableEntity(),
            new Object[]{"이름", false}, new Object[]{"이름", true});

        // when
        listener.onPreUpdate(event);

        // then
        assertThat(context.getDetails()).hasSize(1);
        assertThat(context.getDetails().get(0).getActionType()).isEqualTo(ChangeActionType.DELETE);
    }

    @Test
    void SoftDeletable_엔티티의_isDeleted가_false로_바뀌면_RESTORE로_분류된다() {
        // given
        AuditContext context = new AuditContext("DOMAIN", ChangeActionType.UPDATE, "1", null);
        AuditContextHolder.set(context);
        PreUpdateEvent event = updateEvent(new AuditableSoftDeletableEntity(),
            new Object[]{"이름", true}, new Object[]{"이름", false});

        // when
        listener.onPreUpdate(event);

        // then
        assertThat(context.getDetails()).hasSize(1);
        assertThat(context.getDetails().get(0).getActionType()).isEqualTo(ChangeActionType.RESTORE);
    }

    @Test
    void SoftDeletable가_아니면_isDeleted_변화와_무관하게_UPDATE로_분류된다() {
        // given
        AuditContext context = new AuditContext("DOMAIN", ChangeActionType.UPDATE, "1", null);
        AuditContextHolder.set(context);
        PreUpdateEvent event = updateEvent(new AuditableEntity(),
            new Object[]{"이름", false}, new Object[]{"이름2", false});

        // when
        listener.onPreUpdate(event);

        // then
        assertThat(context.getDetails().get(0).getActionType()).isEqualTo(ChangeActionType.UPDATE);
    }

    private PreUpdateEvent updateEvent(Object entity, Object[] oldState, Object[] newState) {
        EntityPersister persister = mock(EntityPersister.class);
        lenient().when(persister.getPropertyNames()).thenReturn(PROPERTY_NAMES);
        return new PreUpdateEvent(entity, 1L, newState, oldState, persister, mock(EventSource.class));
    }
}
