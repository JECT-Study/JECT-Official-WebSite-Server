package org.ject.support.domain.member;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ActivityStatusTest {

    @Test
    @DisplayName("활동 상태가 ACTIVE이면 true를 반환한다")
    void 활동_상태가_ACTIVE이면_true를_반환한다() {
        // when
        boolean result = ActivityStatus.isActive(ActivityStatus.ACTIVE);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("활동 상태가 ACTIVE가 아니면 false를 반환한다")
    void 활동_상태가_ACTIVE가_아니면_false를_반환한다() {
        // when
        boolean result = ActivityStatus.isActive(ActivityStatus.ENDED);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("활동 상태가 null이면 false를 반환한다")
    void 활동_상태가_null이면_false를_반환한다() {
        // when
        boolean result = ActivityStatus.isActive(null);

        // then
        assertThat(result).isFalse();
    }

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
