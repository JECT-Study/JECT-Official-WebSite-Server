package org.ject.support.common.audit;

import static org.assertj.core.api.Assertions.assertThat;

import org.ject.support.base.UnitTestSupport;
import org.ject.support.domain.changehistory.domain.ChangeActionType;
import org.ject.support.domain.changehistory.domain.ChangeDetail;
import org.junit.jupiter.api.Test;

class AuditContextTest extends UnitTestSupport {

    @Test
    void 컨텍스트에_작업_메타데이터가_보관된다() {
        // given & when
        AuditContext context = new AuditContext("MEMBER", ChangeActionType.UPDATE, "7", 10L);

        // then
        assertThat(context.getDomain()).isEqualTo("MEMBER");
        assertThat(context.getAction()).isEqualTo(ChangeActionType.UPDATE);
        assertThat(context.getTargetId()).isEqualTo("7");
        assertThat(context.getActorId()).isEqualTo(10L);
        assertThat(context.getDetails()).isEmpty();
    }

    @Test
    void 수집된_변경_상세가_누적된다() {
        // given
        AuditContext context = new AuditContext("MEMBER", ChangeActionType.UPDATE, "7", null);
        ChangeDetail first = ChangeDetail.create("Member", "1", ChangeActionType.UPDATE, "{}", "{}");
        ChangeDetail second = ChangeDetail.create("Member", "2", ChangeActionType.UPDATE, "{}", "{}");

        // when
        context.addDetail(first);
        context.addDetail(second);

        // then
        assertThat(context.getDetails()).containsExactly(first, second);
    }
}
