package org.ject.support.domain.member;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ActivityStatusTest {

    @Test
    @DisplayName("일반 구성원은 활동 중 완주 탈퇴 상태를 사용할 수 있다")
    void 일반_구성원은_활동_중_완주_탈퇴_상태를_사용할_수_있다() {
        // given
        List<ActivityStatus> statuses = List.of(
            ActivityStatus.ACTIVE,
            ActivityStatus.COMPLETED,
            ActivityStatus.WITHDRAWN
        );

        // when
        boolean result = ActivityStatus.isAllAvailableFor(statuses, MemberType.SEMESTER);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("일반 구성원은 활동 종료 중도 이탈 상태를 사용할 수 없다")
    void 일반_구성원은_활동_종료_중도_이탈_상태를_사용할_수_없다() {
        // given
        List<ActivityStatus> statuses = List.of(
            ActivityStatus.ENDED,
            ActivityStatus.DROPOUT
        );

        // when
        boolean result = ActivityStatus.isAllAvailableFor(statuses, MemberType.SEMESTER);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("메이커스는 활동 중 활동 종료 중도 이탈 상태를 사용할 수 있다")
    void 메이커스는_활동_중_활동_종료_중도_이탈_상태를_사용할_수_있다() {
        // given
        List<ActivityStatus> statuses = List.of(
            ActivityStatus.ACTIVE,
            ActivityStatus.ENDED,
            ActivityStatus.DROPOUT
        );

        // when
        boolean makersResult = ActivityStatus.isAllAvailableFor(statuses, MemberType.MAKERS);

        // then
        assertThat(makersResult).isTrue();
    }

    @Test
    @DisplayName("메이커스는 완주 탈퇴 상태를 사용할 수 없다")
    void 메이커스는_완주_탈퇴_상태를_사용할_수_없다() {
        // given
        List<ActivityStatus> statuses = List.of(
            ActivityStatus.COMPLETED,
            ActivityStatus.WITHDRAWN
        );

        // when
        boolean makersResult = ActivityStatus.isAllAvailableFor(statuses, MemberType.MAKERS);

        // then
        assertThat(makersResult).isFalse();
    }

    @Test
    @DisplayName("운영 서포터즈는 활동 중 활동 종료 중도 이탈 상태를 사용할 수 있다")
    void 운영_서포터즈는_활동_중_활동_종료_중도_이탈_상태를_사용할_수_있다() {
        // given
        List<ActivityStatus> statuses = List.of(
            ActivityStatus.ACTIVE,
            ActivityStatus.ENDED,
            ActivityStatus.DROPOUT
        );

        // when
        boolean result = ActivityStatus.isAllAvailableFor(statuses, MemberType.SUPPORTERS);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("운영 서포터즈는 완주 탈퇴 상태를 사용할 수 없다")
    void 운영_서포터즈는_완주_탈퇴_상태를_사용할_수_없다() {
        // given
        List<ActivityStatus> statuses = List.of(
            ActivityStatus.COMPLETED,
            ActivityStatus.WITHDRAWN
        );

        // when
        boolean result = ActivityStatus.isAllAvailableFor(statuses, MemberType.SUPPORTERS);

        // then
        assertThat(result).isFalse();
    }
}
