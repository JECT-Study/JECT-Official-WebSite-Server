package org.ject.support.domain.member.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.ject.support.domain.member.fixture.SemesterActivityFixture.semesterActivity;

import org.ject.support.domain.member.ActivityStatus;
import org.ject.support.domain.member.Availability;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.CareerLevel;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MakersTeam;
import org.ject.support.domain.member.MemberType;
import org.ject.support.domain.member.exception.MemberErrorCode;
import org.ject.support.domain.member.exception.MemberException;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberActivityTest {

	@Test
	@DisplayName("일반 구성원 활동을 생성한다")
	void 일반_구성원_활동을_생성한다() {
		// given

		// when
		MemberActivity memberActivity = semesterActivity().build();

		// then
		assertThat(memberActivity.getMemberId()).isEqualTo(1L);
		assertThat(memberActivity.getMemberType()).isEqualTo(MemberType.SEMESTER);
		assertThat(memberActivity.getJobFamily()).isEqualTo(JobFamily.BE);
		assertThat(memberActivity.getRecruitTypeDetail()).isEqualTo(RecruitTypeDetail.REGULAR);
		assertThat(memberActivity.getCareerDetails()).isEqualTo(CareerDetails.EMPLOYEE);
		assertThat(memberActivity.getExperiencePeriod()).isEqualTo(ExperiencePeriod.ONE_TO_TWO);
		assertThat(memberActivity.getMemo()).isEqualTo("테스트 메모");

	}

	@Test
	@DisplayName("일반 구성원 활동은 ACTIVE 상태로 생성된다")
	void 일반_구성원_활동은_ACTIVE_상태로_생성된다() {
		// given

		// when
		MemberActivity memberActivity = semesterActivity().build();

		// then
		assertThat(memberActivity.getActivityStatus()).isEqualTo(ActivityStatus.ACTIVE);
		assertThat(memberActivity.getIsDeleted()).isFalse();
	}

	@Test
	@DisplayName("일반 구성원 활동을 생성하면 관리 항목이 함께 생성된다")
	void 일반_구성원_활동을_생성하면_관리_항목이_함께_생성된다() {
		// given

		// when
		MemberActivity memberActivity = semesterActivity().build();
		MemberSemester memberSemester = memberActivity.getMemberSemester();

		// then
		assertThat(memberSemester).isNotNull();
		assertThat(memberSemester.getSemesterId()).isEqualTo(2L);
		assertThat(memberSemester.getTeamId()).isEqualTo(3L);
		assertThat(memberSemester.getMemberActivity()).isSameAs(memberActivity);
	}

	@Test
	@DisplayName("메이커스 구성원 활동을 생성하면 관리 항목이 함께 생성된다")
	void 메이커스_구성원_활동을_생성하면_관리_항목이_함께_생성된다() {
		// when
		MemberActivity memberActivity = MemberActivity.createMakersActivity(
			1L,
			JobFamily.FE,
			RecruitTypeDetail.REGULAR,
			CareerDetails.EMPLOYEE,
			ExperiencePeriod.ONE_TO_TWO,
			"테스트 메모",
			MakersTeam.TEAM_1,
			Availability.HIGHLY_AVAILABLE,
			Availability.AVAILABLE_BY_TOPIC,
			Availability.CONSIDER_LATER,
			CareerLevel.JUNIOR,
			"Spring",
			"JECT",
			"백오피스",
			"MK-001"
		);
		MemberMakers memberMakers = memberActivity.getMemberMakers();

		// then
		assertThat(memberActivity.getMemberId()).isEqualTo(1L);
		assertThat(memberActivity.getMemberType()).isEqualTo(MemberType.MAKERS);
		assertThat(memberActivity.getJobFamily()).isEqualTo(JobFamily.FE);
		assertThat(memberActivity.getRecruitTypeDetail()).isEqualTo(RecruitTypeDetail.REGULAR);
		assertThat(memberActivity.getCareerDetails()).isEqualTo(CareerDetails.EMPLOYEE);
		assertThat(memberActivity.getExperiencePeriod()).isEqualTo(ExperiencePeriod.ONE_TO_TWO);
		assertThat(memberActivity.getMemo()).isEqualTo("테스트 메모");
		assertThat(memberMakers).isNotNull();
		assertThat(memberMakers.getMemberActivity()).isSameAs(memberActivity);
		assertThat(memberMakers.getMakersTeam()).isEqualTo(MakersTeam.TEAM_1);
		assertThat(memberMakers.getMentoringAvailability()).isEqualTo(Availability.HIGHLY_AVAILABLE);
		assertThat(memberMakers.getProjectSupplementAvailability()).isEqualTo(Availability.AVAILABLE_BY_TOPIC);
		assertThat(memberMakers.getSpeakerAvailability()).isEqualTo(Availability.CONSIDER_LATER);
		assertThat(memberMakers.getCareerLevel()).isEqualTo(CareerLevel.JUNIOR);
		assertThat(memberMakers.getSkills()).isEqualTo("Spring");
		assertThat(memberMakers.getCompany()).isEqualTo("JECT");
		assertThat(memberMakers.getExpertTopics()).isEqualTo("백오피스");
		assertThat(memberMakers.getActivityCertNumber()).isEqualTo("MK-001");
	}

	@Test
	@DisplayName("팀을 선택하지 않으면 팀 정보 없이 일반 구성원 활동을 생성한다")
	void 팀을_선택하지_않으면_팀_정보_없이_일반_구성원_활동을_생성한다() {
		// given

		// when
		MemberActivity memberActivity = semesterActivity().teamId(null).build();

		// then
		assertThat(memberActivity.getMemberSemester().getTeamId()).isNull();
	}

	@Test
	@DisplayName("일반 구성원에서 사용할 수 없는 활동 상태로 변경하면 예외가 발생한다")
	void 일반_구성원에서_사용할_수_없는_활동_상태로_변경하면_예외가_발생한다() {
		// given
		MemberActivity memberActivity = semesterActivity().build();

		// when
		Throwable throwable = catchThrowable(() ->
			memberActivity.updateActivityStatus(ActivityStatus.DROPOUT)
		);

		// then
		assertThat(throwable)
			.isInstanceOf(MemberException.class)
			.extracting("errorCode")
			.isEqualTo(MemberErrorCode.INVALID_ACTIVITY_STATUS);
	}
}
