package org.ject.support.domain.member.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ject.support.domain.member.fixture.MakersActivityFixture.makersActivity;

import org.ject.support.domain.member.Availability;
import org.ject.support.domain.member.CareerLevel;
import org.ject.support.domain.member.MakersTeam;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberMakersTest {

	@Test
	@DisplayName("입력한 메이커스팀 구성원 상세정보만 변경되고 입력하지 않은 상세정보는 유지된다")
	void 입력한_메이커스팀_구성원_상세정보만_변경되고_입력하지_않은_상세정보는_유지된다() {
		// given
		MemberMakers memberMakers = makersActivity().build().getMemberMakers();
		// when
		memberMakers.edit(MakersTeam.TEAM_2, null, null, null, null, null, "수정된 회사", null, null);

		// then
		assertThat(memberMakers.getMakersTeam()).isEqualTo(MakersTeam.TEAM_2);
		assertThat(memberMakers.getCompany()).isEqualTo("수정된 회사");
		assertThat(memberMakers.getMentoringAvailability()).isEqualTo(Availability.HIGHLY_AVAILABLE);
		assertThat(memberMakers.getCareerLevel()).isEqualTo(CareerLevel.JUNIOR);
		assertThat(memberMakers.getSkills()).isEqualTo("Spring");
	}

	@Test
	@DisplayName("메이커스팀 구성원의 소속과 활동 가능 정보와 경력 정보를 함께 변경할 수 있다")
	void 메이커스팀_구성원의_소속과_활동_가능_정보와_경력_정보를_함께_변경할_수_있다() {
		// given
		MemberMakers memberMakers = makersActivity().build().getMemberMakers();
		// when
		memberMakers.edit(
			MakersTeam.TEAM_2, Availability.UNAVAILABLE, Availability.CONSIDER_LATER,
			Availability.AVAILABLE_BY_TOPIC, CareerLevel.SENIOR, "Kotlin", "젝트", "아키텍처", "MK-002");

		// then
		assertThat(memberMakers.getMakersTeam()).isEqualTo(MakersTeam.TEAM_2);
		assertThat(memberMakers.getMentoringAvailability()).isEqualTo(Availability.UNAVAILABLE);
		assertThat(memberMakers.getProjectSupplementAvailability()).isEqualTo(Availability.CONSIDER_LATER);
		assertThat(memberMakers.getSpeakerAvailability()).isEqualTo(Availability.AVAILABLE_BY_TOPIC);
		assertThat(memberMakers.getCareerLevel()).isEqualTo(CareerLevel.SENIOR);
		assertThat(memberMakers.getSkills()).isEqualTo("Kotlin");
		assertThat(memberMakers.getCompany()).isEqualTo("젝트");
		assertThat(memberMakers.getExpertTopics()).isEqualTo("아키텍처");
		assertThat(memberMakers.getActivityCertNumber()).isEqualTo("MK-002");
	}
}
