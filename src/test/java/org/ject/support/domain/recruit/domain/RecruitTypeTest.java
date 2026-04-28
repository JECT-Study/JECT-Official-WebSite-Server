package org.ject.support.domain.recruit.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RecruitTypeTest {

    @Test
    void 신규_모집_유형과_기존_모집_유형을_함께_제공한다() {
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
    void 모집_사유_값을_제공한다() {
        assertThat(RecruitTypeDetail.values())
                .contains(
                        RecruitTypeDetail.REGULAR,
                        RecruitTypeDetail.NEW,
                        RecruitTypeDetail.REFILL
                );
    }

    @Test
    void 모집_유형별_허용_모집_사유를_판별한다() {
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
    void 기존_모집_유형은_호환_기간_동안_모집_사유_검증에서_차단하지_않는다() {
        assertThat(RecruitType.REGULAR.supports(RecruitTypeDetail.REGULAR)).isTrue();
        assertThat(RecruitType.REGULAR_WAITLIST.supports(RecruitTypeDetail.REGULAR)).isTrue();
        assertThat(RecruitType.BACKFILL.supports(RecruitTypeDetail.REFILL)).isTrue();
        assertThat(RecruitType.MANUAL.supports(RecruitTypeDetail.REGULAR)).isTrue();
    }
}
