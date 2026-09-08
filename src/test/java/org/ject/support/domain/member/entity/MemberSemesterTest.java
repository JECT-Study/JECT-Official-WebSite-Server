package org.ject.support.domain.member.entity;

import static org.assertj.core.api.Assertions.*;
import static org.ject.support.domain.member.fixture.SemesterActivityFixture.semesterActivity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberSemesterTest {

    @Test
    @DisplayName("일반 구성원의 활동정보를 수정한다")
    void 일반_구성원의_활동정보를_수정한다() {
        // given
        MemberSemester memberSemester = semesterActivity().build().getMemberSemester();

        // when
        memberSemester.update("SEMESTER-EDIT", "https://review/1", "https://review/2");

        // then
        assertThat(memberSemester.getCertNumber()).isEqualTo("SEMESTER-EDIT");
        assertThat(memberSemester.getFirstReview()).isEqualTo("https://review/1");
        assertThat(memberSemester.getSecondReview()).isEqualTo("https://review/2");
    }

    @Test
    @DisplayName("입력하지 않은 일반 구성원 활동정보는 기존 값을 유지한다")
    void 입력하지_않은_일반_구성원_활동정보는_기존_값을_유지한다() {
        // given
        MemberSemester memberSemester = semesterActivity().build().getMemberSemester();
        Long semesterId = memberSemester.getSemesterId();
        Long teamId = memberSemester.getTeamId();

        // when
        memberSemester.update(null, null, null);

        // then
        assertThat(memberSemester.getSemesterId()).isEqualTo(semesterId);
        assertThat(memberSemester.getTeamId()).isEqualTo(teamId);
    }

    @Test
    @DisplayName("일반 구성원의 기수만 변경한다")
    void 일반_구성원의_기수만_변경한다() {
        // given
        MemberSemester memberSemester = semesterActivity().semesterId(1L).teamId(2L).build().getMemberSemester();

        // when
        memberSemester.changeSemester(3L);

        // then
        assertThat(memberSemester.getSemesterId()).isEqualTo(3L);
        assertThat(memberSemester.getTeamId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("일반 구성원의 팀을 미지정 상태로 변경한다")
    void 일반_구성원의_팀을_미지정_상태로_변경한다() {
        // given
        MemberSemester memberSemester = semesterActivity().teamId(2L).build().getMemberSemester();

        // when
        memberSemester.changeTeam(null);

        // then
        assertThat(memberSemester.getTeamId()).isNull();
    }
}
