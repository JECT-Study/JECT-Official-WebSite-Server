package org.ject.support.domain.recruit.domain;

import org.ject.support.domain.member.JobFamily;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ject.support.domain.member.MemberType.MAKERS;
import static org.ject.support.domain.member.MemberType.SEMESTER;
import static org.ject.support.domain.member.MemberType.SUPPORTERS;

class RecruitTypeTest {

    @Test
    @DisplayName("모집 유형별 지원 가능한 직군을 판별한다")
    void 모집_유형별_지원_가능한_직군을_판별한다() {
        assertThat(JobFamily.values())
                .filteredOn(jobFamily -> RecruitType.SEMESTER.supports(jobFamily))
                .containsExactlyInAnyOrder(JobFamily.FE, JobFamily.BE, JobFamily.APP, JobFamily.PM, JobFamily.PD);
        assertThat(JobFamily.values())
                .filteredOn(jobFamily -> RecruitType.MAKERS.supports(jobFamily))
                .containsExactlyInAnyOrder(JobFamily.FE, JobFamily.BE, JobFamily.PM, JobFamily.PD);
        assertThat(JobFamily.values())
                .filteredOn(jobFamily -> RecruitType.SUPPORTERS.supports(jobFamily))
                .containsExactlyInAnyOrder(JobFamily.BX, JobFamily.INFRA, JobFamily.ER, JobFamily.OPS);
    }

    @Test
    void 지원하는_모집_유형을_제공한다() {
        assertThat(RecruitType.values())
                .containsExactly(
                        RecruitType.SEMESTER,
                        RecruitType.MAKERS,
                        RecruitType.SUPPORTERS
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
    void 모집_유형을_구성원_타입으로_변환한다() {
        assertThat(RecruitType.SEMESTER.toMemberType()).isEqualTo(SEMESTER);
        assertThat(RecruitType.MAKERS.toMemberType()).isEqualTo(MAKERS);
        assertThat(RecruitType.SUPPORTERS.toMemberType()).isEqualTo(SUPPORTERS);
    }
}
