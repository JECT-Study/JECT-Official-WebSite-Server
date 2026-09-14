package org.ject.support.domain.member.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.ject.support.domain.member.fixture.MakersActivityFixture.makersActivity;
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
	@DisplayName("일반 구성원 활동은 요청한 상태로 생성된다")
	void 일반_구성원_활동은_요청한_상태로_생성된다() {
		// given

		// when
		MemberActivity memberActivity = semesterActivity()
			.activityStatus(ActivityStatus.COMPLETED)
			.build();

		// then
		assertThat(memberActivity.getActivityStatus()).isEqualTo(ActivityStatus.COMPLETED);
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
			ActivityStatus.ENDED,
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
		assertThat(memberActivity.getActivityStatus()).isEqualTo(ActivityStatus.ENDED);
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
	@DisplayName("일반 구성원에 맞지 않는 활동 상태로 생성하면 예외가 발생한다")
	void 일반_구성원에_맞지_않는_활동_상태로_생성하면_예외가_발생한다() {
		// when
		Throwable throwable = catchThrowable(() -> semesterActivity()
			.activityStatus(ActivityStatus.DROPOUT)
			.build());

		// then
		assertThat(throwable)
			.isInstanceOf(MemberException.class)
			.extracting("errorCode")
			.isEqualTo(MemberErrorCode.INVALID_ACTIVITY_STATUS);
	}

	@Test
	@DisplayName("메이커스팀에 맞지 않는 활동 상태로 생성하면 예외가 발생한다")
	void 메이커스팀에_맞지_않는_활동_상태로_생성하면_예외가_발생한다() {
		// when
		Throwable throwable = catchThrowable(() -> makersActivity()
			.activityStatus(ActivityStatus.COMPLETED)
			.build());

		// then
		assertThat(throwable)
			.isInstanceOf(MemberException.class)
			.extracting("errorCode")
			.isEqualTo(MemberErrorCode.INVALID_ACTIVITY_STATUS);
	}

	@Test
	@DisplayName("운영 서포터즈 활동을 생성하면 관리 항목이 함께 생성된다")
	void 운영_서포터즈_활동을_생성하면_관리_항목이_함께_생성된다() {
		// when
		MemberActivity memberActivity = MemberActivity.createSupportersActivity(
			1L,
			JobFamily.OPS,
			RecruitTypeDetail.REGULAR,
			ActivityStatus.ENDED,
			java.time.LocalDate.of(2025, 5, 19),
			java.time.LocalDate.of(2025, 12, 19),
			"SP-001",
			"운영 서포터즈 메모"
		);
		MemberSupporters memberSupporters = memberActivity.getMemberSupporters();

		// then
		assertThat(memberActivity.getMemberId()).isEqualTo(1L);
		assertThat(memberActivity.getMemberType()).isEqualTo(MemberType.SUPPORTERS);
		assertThat(memberActivity.getJobFamily()).isEqualTo(JobFamily.OPS);
		assertThat(memberActivity.getRecruitTypeDetail()).isEqualTo(RecruitTypeDetail.REGULAR);
		assertThat(memberActivity.getActivityStatus()).isEqualTo(ActivityStatus.ENDED);
		assertThat(memberActivity.getStartDate()).isEqualTo(java.time.LocalDate.of(2025, 5, 19));
		assertThat(memberActivity.getEndDate()).isEqualTo(java.time.LocalDate.of(2025, 12, 19));
		assertThat(memberActivity.getMemo()).isEqualTo("운영 서포터즈 메모");
		assertThat(memberSupporters).isNotNull();
		assertThat(memberSupporters.getMemberActivity()).isSameAs(memberActivity);
		assertThat(memberSupporters.getActivityCertNumber()).isEqualTo("SP-001");
	}

	@Test
	@DisplayName("구성원 유형에 맞지 않는 포지션으로 활동을 생성하면 예외가 발생한다")
	void 구성원_유형에_맞지_않는_포지션으로_활동을_생성하면_예외가_발생한다() {
		// when
		Throwable throwable = catchThrowable(() -> MemberActivity.createSupportersActivity(
			1L,
			JobFamily.FE,
			RecruitTypeDetail.REGULAR,
			ActivityStatus.ACTIVE,
			null,
			null,
			null,
			null
		));

		// then
		assertThat(throwable)
			.isInstanceOf(MemberException.class)
			.extracting("errorCode")
			.isEqualTo(MemberErrorCode.INVALID_JOB_FAMILY);
	}

	@Test
	@DisplayName("운영 서포터즈에 맞지 않는 활동 상태로 생성하면 예외가 발생한다")
	void 운영_서포터즈에_맞지_않는_활동_상태로_생성하면_예외가_발생한다() {
		// when
		Throwable throwable = catchThrowable(() -> MemberActivity.createSupportersActivity(
			1L,
			JobFamily.OPS,
			RecruitTypeDetail.REGULAR,
			ActivityStatus.COMPLETED,
			null,
			null,
			null,
			null
		));

		// then
		assertThat(throwable)
			.isInstanceOf(MemberException.class)
			.extracting("errorCode")
			.isEqualTo(MemberErrorCode.INVALID_ACTIVITY_STATUS);
	}

	@Test
	@DisplayName("직군이 없으면 예외가 발생한다")
	void 직군이_없으면_예외가_발생한다() {
		// when
		Throwable throwable = catchThrowable(() -> MemberActivity.createSupportersActivity(
			1L,
			null,
			RecruitTypeDetail.REGULAR,
			ActivityStatus.ACTIVE,
			null,
			null,
			null,
			null
		));

		// then
		assertThat(throwable)
			.isInstanceOf(MemberException.class)
			.extracting("errorCode")
			.isEqualTo(MemberErrorCode.INVALID_JOB_FAMILY);
	}

	@Test
	@DisplayName("활동 상태가 없으면 예외가 발생한다")
	void 활동_상태가_없으면_예외가_발생한다() {
		// when
		Throwable throwable = catchThrowable(() -> MemberActivity.createSupportersActivity(
			1L,
			JobFamily.OPS,
			RecruitTypeDetail.REGULAR,
			null,
			null,
			null,
			null,
			null
		));

		// then
		assertThat(throwable)
			.isInstanceOf(MemberException.class)
			.extracting("errorCode")
			.isEqualTo(MemberErrorCode.INVALID_ACTIVITY_STATUS);
	}

	@Test
	@DisplayName("활동 종료일이 시작일보다 빠르면 예외가 발생한다")
	void 활동_종료일이_시작일보다_빠르면_예외가_발생한다() {
		// when
		Throwable throwable = catchThrowable(() -> MemberActivity.createSupportersActivity(
			1L,
			JobFamily.OPS,
			RecruitTypeDetail.REGULAR,
			ActivityStatus.ENDED,
			java.time.LocalDate.of(2025, 12, 19),
			java.time.LocalDate.of(2025, 5, 19),
			null,
			null
		));

		// then
		assertThat(throwable)
			.isInstanceOf(MemberException.class)
			.extracting("errorCode")
			.isEqualTo(MemberErrorCode.INVALID_ACTIVITY_PERIOD);
	}

	@Test
	@DisplayName("시작일 없이 종료일만 있으면 예외가 발생한다")
	void 시작일_없이_종료일만_있으면_예외가_발생한다() {
		// when
		Throwable throwable = catchThrowable(() -> MemberActivity.createSupportersActivity(
			1L,
			JobFamily.OPS,
			RecruitTypeDetail.REGULAR,
			ActivityStatus.ENDED,
			null,
			java.time.LocalDate.of(2025, 12, 19),
			null,
			null
		));

		// then
		assertThat(throwable)
			.isInstanceOf(MemberException.class)
			.extracting("errorCode")
			.isEqualTo(MemberErrorCode.INVALID_ACTIVITY_PERIOD);
	}

	@Test
	@DisplayName("종료일이 없거나 시작일과 같으면 활동 기간을 허용한다")
	void 종료일이_없거나_시작일과_같으면_활동_기간을_허용한다() {
		java.time.LocalDate activityDate = java.time.LocalDate.of(2025, 5, 19);

		assertThatCode(() -> MemberActivity.createSupportersActivity(
			1L, JobFamily.OPS, RecruitTypeDetail.REGULAR, ActivityStatus.ACTIVE,
			null, null, null, null
		)).doesNotThrowAnyException();
		assertThatCode(() -> MemberActivity.createSupportersActivity(
			1L, JobFamily.OPS, RecruitTypeDetail.REGULAR, ActivityStatus.ACTIVE,
			activityDate, null, null, null
		)).doesNotThrowAnyException();
		assertThatCode(() -> MemberActivity.createSupportersActivity(
			1L, JobFamily.OPS, RecruitTypeDetail.REGULAR, ActivityStatus.ENDED,
			activityDate, activityDate, null, null
		)).doesNotThrowAnyException();
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

	@Test
	@DisplayName("입력한 활동정보만 변경되고 입력하지 않은 활동정보는 유지된다")
	void 입력한_활동정보만_변경되고_입력하지_않은_활동정보는_유지된다() {
		// given
		MemberActivity memberActivity = makersActivity().build();
		// when
		memberActivity.edit(JobFamily.BE, null, RecruitTypeDetail.REFILL, null, null);

		// then
		assertThat(memberActivity.getJobFamily()).isEqualTo(JobFamily.BE);
		assertThat(memberActivity.getRecruitTypeDetail()).isEqualTo(RecruitTypeDetail.REFILL);
		assertThat(memberActivity.getCareerDetails()).isEqualTo(CareerDetails.EMPLOYEE);
		assertThat(memberActivity.getExperiencePeriod()).isEqualTo(ExperiencePeriod.ONE_TO_TWO);
		assertThat(memberActivity.getMemo()).isEqualTo("테스트 메모");
	}

	@Test
	@DisplayName("메이커스팀 구성원에게 허용되지 않는 직군으로 변경할 수 없다")
	void 메이커스팀_구성원에게_허용되지_않는_직군으로_변경할_수_없다() {
		// given
		MemberActivity memberActivity = makersActivity().build();
		// when
		Throwable throwable = catchThrowable(() -> memberActivity.edit(JobFamily.OPS, null, null, null, null));

		// then
		assertThat(throwable)
			.isInstanceOf(MemberException.class)
			.extracting("errorCode")
			.isEqualTo(MemberErrorCode.INVALID_JOB_FAMILY);
	}

	@Test
	@DisplayName("메이커스팀 구성원의 활동정보와 상세정보를 함께 수정할 수 있다")
	void 메이커스팀_구성원의_활동정보와_상세정보를_함께_수정할_수_있다() {
		// given
		MemberActivity memberActivity = makersActivity().build();
		// when
		memberActivity.editMakersActivity(
			JobFamily.BE, CareerDetails.JOB_SEEKER, null, null, "수정된 메모",
			MakersTeam.TEAM_2, null, null, null, null, null, "수정된 회사", null, null);

		// then
		assertThat(memberActivity.getJobFamily()).isEqualTo(JobFamily.BE);
		assertThat(memberActivity.getCareerDetails()).isEqualTo(CareerDetails.JOB_SEEKER);
		assertThat(memberActivity.getMemo()).isEqualTo("수정된 메모");
		assertThat(memberActivity.getMemberMakers().getMakersTeam()).isEqualTo(MakersTeam.TEAM_2);
		assertThat(memberActivity.getMemberMakers().getCompany()).isEqualTo("수정된 회사");
	}

	@Test
	@DisplayName("메이커스팀 구성원 활동을 활동 중 상태로 변경할 수 있다")
	void 메이커스팀_구성원_활동을_활동_중_상태로_변경할_수_있다() {
		MemberActivity memberActivity = makersActivity().activityStatus(ActivityStatus.ENDED).build();

		memberActivity.activate();

		assertThat(memberActivity.getActivityStatus()).isEqualTo(ActivityStatus.ACTIVE);
	}

	@Test
	@DisplayName("메이커스팀 구성원 활동을 활동 종료 상태로 변경할 수 있다")
	void 메이커스팀_구성원_활동을_활동_종료_상태로_변경할_수_있다() {
		MemberActivity memberActivity = makersActivity().build();

		memberActivity.end();

		assertThat(memberActivity.getActivityStatus()).isEqualTo(ActivityStatus.ENDED);
	}

	@Test
	@DisplayName("메이커스팀 구성원 활동을 중도 이탈 상태로 변경할 수 있다")
	void 메이커스팀_구성원_활동을_중도_이탈_상태로_변경할_수_있다() {
		MemberActivity memberActivity = makersActivity().build();

		memberActivity.dropOut();

		assertThat(memberActivity.getActivityStatus()).isEqualTo(ActivityStatus.DROPOUT);
	}

	@Test
	@DisplayName("메이커스팀 구성원에게 허용되지 않는 활동 상태로 변경할 수 없다")
	void 메이커스팀_구성원에게_허용되지_않는_활동_상태로_변경할_수_없다() {
		// given
		MemberActivity memberActivity = makersActivity().build();

		// when
		Throwable throwable = catchThrowable(() -> memberActivity.updateActivityStatus(ActivityStatus.COMPLETED));

		// then
		assertThat(throwable)
			.isInstanceOf(MemberException.class)
			.extracting("errorCode")
			.isEqualTo(MemberErrorCode.INVALID_ACTIVITY_STATUS);
	}

	@Test
	@DisplayName("입력한 운영 서포터즈 구성원 활동정보만 변경되고 입력하지 않은 활동정보는 유지된다")
	void 입력한_운영_서포터즈_구성원_활동정보만_변경되고_입력하지_않은_활동정보는_유지된다() {
		// given
		MemberActivity memberActivity = supportersActivity();
		// when
		memberActivity.editSupportersActivity(
			JobFamily.INFRA, null, java.time.LocalDate.of(2026, 2, 1), null, "수정된 메모", null);

		// then
		assertThat(memberActivity.getJobFamily()).isEqualTo(JobFamily.INFRA);
		assertThat(memberActivity.getRecruitTypeDetail()).isEqualTo(RecruitTypeDetail.REGULAR);
		assertThat(memberActivity.getStartDate()).isEqualTo(java.time.LocalDate.of(2026, 2, 1));
		assertThat(memberActivity.getEndDate()).isEqualTo(java.time.LocalDate.of(2026, 12, 31));
		assertThat(memberActivity.getMemo()).isEqualTo("수정된 메모");
	}

	@Test
	@DisplayName("운영 서포터즈 구성원에게 허용되지 않는 직군으로 변경할 수 없다")
	void 운영_서포터즈_구성원에게_허용되지_않는_직군으로_변경할_수_없다() {
		// given
		MemberActivity memberActivity = supportersActivity();
		// when
		Throwable throwable = catchThrowable(() ->
			memberActivity.editSupportersActivity(JobFamily.FE, null, null, null, null, null));

		// then
		assertThat(throwable)
			.isInstanceOf(MemberException.class)
			.extracting("errorCode")
			.isEqualTo(MemberErrorCode.INVALID_JOB_FAMILY);
	}

	@Test
	@DisplayName("운영 서포터즈 구성원의 활동 종료일을 시작일보다 빠르게 변경할 수 없다")
	void 운영_서포터즈_구성원의_활동_종료일을_시작일보다_빠르게_변경할_수_없다() {
		// given
		MemberActivity memberActivity = supportersActivity();
		// when
		Throwable throwable = catchThrowable(() ->
			memberActivity.editSupportersActivity(
				null, null, null, java.time.LocalDate.of(2025, 12, 31), null, null));

		// then
		assertThat(throwable)
			.isInstanceOf(MemberException.class)
			.extracting("errorCode")
			.isEqualTo(MemberErrorCode.INVALID_ACTIVITY_PERIOD);
	}

	private MemberActivity supportersActivity() {
		return MemberActivity.createSupportersActivity(
			1L,
			JobFamily.OPS,
			RecruitTypeDetail.REGULAR,
			ActivityStatus.ACTIVE,
			java.time.LocalDate.of(2026, 1, 1),
			java.time.LocalDate.of(2026, 12, 31),
			"SP-001",
			"운영 서포터즈 메모"
		);
	}
}
