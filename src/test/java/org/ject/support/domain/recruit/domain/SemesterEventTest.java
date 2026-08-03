package org.ject.support.domain.recruit.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ject.support.domain.recruit.domain.SemesterEventType.EVENT;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SemesterEventTest {

    @Test
    @DisplayName("기수 행사를 생성한다")
    void 기수_행사를_생성한다() {
        // given
        Long semesterId = 4L;
        String name = "오리엔테이션";

        // when
        SemesterEvent semesterEvent = SemesterEvent.create(semesterId, EVENT, name);

        // then
        assertThat(semesterEvent.getSemesterId()).isEqualTo(semesterId);
        assertThat(semesterEvent.getType()).isEqualTo(EVENT);
        assertThat(semesterEvent.getName()).isEqualTo(name);
    }

    @Test
    @DisplayName("기수 행사를 생성하면 필수 행사로 설정된다")
    void 기수_행사를_생성하면_필수_행사로_설정된다() {
        // when
        SemesterEvent semesterEvent = SemesterEvent.create(4L, EVENT, "오리엔테이션");

        // then
        assertThat(semesterEvent.getIsRequired()).isTrue();
    }

    @Test
    @DisplayName("기수 행사 이름을 수정한다")
    void 기수_행사_이름을_수정한다() {
        // given
        SemesterEvent semesterEvent = SemesterEvent.create(4L, EVENT, "오리엔테이션");

        // when
        semesterEvent.updateName("사전 오리엔테이션");

        // then
        assertThat(semesterEvent.getName()).isEqualTo("사전 오리엔테이션");
        assertThat(semesterEvent.getSemesterId()).isEqualTo(4L);
        assertThat(semesterEvent.getType()).isEqualTo(EVENT);
    }
}
