package org.ject.support.domain.changehistory.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.ject.support.base.TestSupport;
import org.junit.jupiter.api.Test;

class ChangeHistoryTest extends TestSupport {

    @Test
    void 변경이력_생성_시_작업_메타데이터가_설정된다() {
        // given & when
        ChangeHistory changeHistory = ChangeHistory.create(
            "MEMBER", ChangeActionType.UPDATE, "7", 10L, List.of()
        );

        // then
        assertThat(changeHistory.getDomain()).isEqualTo("MEMBER");
        assertThat(changeHistory.getActionType()).isEqualTo(ChangeActionType.UPDATE);
        assertThat(changeHistory.getTargetId()).isEqualTo("7");
        assertThat(changeHistory.getActorId()).isEqualTo(10L);
    }

    @Test
    void 변경_상세가_이력에_연결되고_역참조가_설정된다() {
        // given
        ChangeDetail detail = ChangeDetail.create("Member", "7", ChangeActionType.UPDATE, "{}", "{}");

        // when
        ChangeHistory changeHistory = ChangeHistory.create(
            "MEMBER", ChangeActionType.UPDATE, "7", 10L, List.of(detail)
        );

        // then
        assertThat(changeHistory.getChangeDetails()).containsExactly(detail);
        assertThat(detail.getChangeHistory()).isSameAs(changeHistory);
    }

    @Test
    void 변경_상세_생성_시_필드가_설정된다() {
        // given & when
        ChangeDetail detail = ChangeDetail.create(
            "Member", "7", ChangeActionType.DELETE, "{\"name\":\"a\"}", null
        );

        // then
        assertThat(detail.getEntityName()).isEqualTo("Member");
        assertThat(detail.getEntityId()).isEqualTo("7");
        assertThat(detail.getActionType()).isEqualTo(ChangeActionType.DELETE);
        assertThat(detail.getBeforeData()).isEqualTo("{\"name\":\"a\"}");
        assertThat(detail.getAfterData()).isNull();
    }
}
