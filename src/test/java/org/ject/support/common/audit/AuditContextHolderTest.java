package org.ject.support.common.audit;

import static org.assertj.core.api.Assertions.assertThat;

import org.ject.support.domain.changehistory.domain.ChangeActionType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class AuditContextHolderTest {

    @AfterEach
    void tearDown() {
        AuditContextHolder.clear();
    }

    @Test
    void 컨텍스트를_설정하면_조회할_수_있다() {
        // given
        AuditContext context = new AuditContext("MEMBER", ChangeActionType.CREATE, null, null);

        // when
        AuditContextHolder.set(context);

        // then
        assertThat(AuditContextHolder.get()).isSameAs(context);
    }

    @Test
    void 컨텍스트를_정리하면_조회되지_않는다() {
        // given
        AuditContextHolder.set(new AuditContext("MEMBER", ChangeActionType.CREATE, null, null));

        // when
        AuditContextHolder.clear();

        // then
        assertThat(AuditContextHolder.get()).isNull();
    }
}
