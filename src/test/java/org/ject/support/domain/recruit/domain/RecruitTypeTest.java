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

    @Test
    @DisplayName("모집 유형별 허용 모집 사유를 판별한다")
    void supports_recruit_type_detail() {
        assertThat(RecruitType.SEMESTER.supports(RecruitTypeDetail.REGULAR)).isTrue();
        assertThat(RecruitType.SEMESTER.supports(RecruitTypeDetail.REFILL)).isTrue();
        assertThat(RecruitType.SEMESTER.supports(RecruitTypeDetail.NEW)).isFalse();

        assertThat(RecruitType.MAKERS.supports(RecruitTypeDetail.NEW)).isTrue();
        assertThat(RecruitType.MAKERS.supports(RecruitTypeDetail.REFILL)).isTrue();
        assertThat(RecruitType.MAKERS.supports(RecruitTypeDetail.REGULAR)).isFalse();

        assertThat(RecruitType.SUPPORTERS.supports(RecruitTypeDetail.NEW)).isTrue();
        assertThat(RecruitType.SUPPORTERS.supports(RecruitTypeDetail.REFILL)).isTrue();
        assertThat(RecruitType.SUPPORTERS.supports(RecruitTypeDetail.REGULAR)).isFalse();
    }

    @Test
    @DisplayName("기존 모집 유형은 호환 기간 동안 모집 사유 validation에서 차단하지 않는다")
    void legacy_recruit_types_support_recruit_type_detail_temporarily() {
        assertThat(RecruitType.REGULAR.supports(RecruitTypeDetail.REGULAR)).isTrue();
        assertThat(RecruitType.REGULAR_WAITLIST.supports(RecruitTypeDetail.REGULAR)).isTrue();
        assertThat(RecruitType.BACKFILL.supports(RecruitTypeDetail.REFILL)).isTrue();
        assertThat(RecruitType.MANUAL.supports(RecruitTypeDetail.REGULAR)).isTrue();
    }
}
