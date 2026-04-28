package org.ject.support.domain.recruit.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RecruitTypeTest {

    @Test
    @DisplayName("신규 모집 유형과 기존 모집 유형을 함께 제공한다")
    void provide_new_recruit_types_with_legacy_types() {
        assertThat(RecruitType.values())
                .contains(
                        RecruitType.SEMESTER,
                        RecruitType.MAKERS,
                        RecruitType.SUPPORTERS,
                        RecruitType.REGULAR,
                        RecruitType.REGULAR_WAITLIST,
                        RecruitType.BACKFILL,
                        RecruitType.MANUAL
                );
    }

    @Test
    @DisplayName("모집 사유 값을 제공한다")
    void provide_recruit_type_detail_values() {
        assertThat(RecruitTypeDetail.values())
                .contains(
                        RecruitTypeDetail.REGULAR,
                        RecruitTypeDetail.NEW,
                        RecruitTypeDetail.REFILL
                );
    }
}
