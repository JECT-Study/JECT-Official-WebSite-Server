package org.ject.support.domain.member.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.ject.support.domain.member.ActivityStatus;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MemberType;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberActivityTest {

	private MemberActivity createSemesterActivity(){
		return MemberActivity.createSemesterActivity(
			1L,
			JobFamily.BE,
			RecruitTypeDetail.REGULAR,
			CareerDetails.EMPLOYEE,
			ExperiencePeriod.ONE_TO_TWO,
			"테스트 메모",
			2L
		);
	}

	@Test
	@DisplayName("일반 구성원 활동을 생성한다.")
	void 일반_구성원_활동을_생성한다() {

		MemberActivity memberActivity = createSemesterActivity();

		assertThat(memberActivity.getMemberId()).isEqualTo(1L);
		assertThat(memberActivity.getMemberType()).isEqualTo(MemberType.SEMESTER);
		assertThat(memberActivity.getJobFamily()).isEqualTo(JobFamily.BE);
		assertThat(memberActivity.getRecruitTypeDetail()).isEqualTo(RecruitTypeDetail.REGULAR);
		assertThat(memberActivity.getCareerDetails()).isEqualTo(CareerDetails.EMPLOYEE);
		assertThat(memberActivity.getExperiencePeriod()).isEqualTo(ExperiencePeriod.ONE_TO_TWO);
		assertThat(memberActivity.getMemo()).isEqualTo("테스트 메모");

	}

	@Test
	@DisplayName("일반_구성원_활동은_ACTIVE_상태로_생성된다")
	void 일반_구성원_활동은_ACTIVE_상태로_생성된다() {
		MemberActivity memberActivity = createSemesterActivity();

		assertThat(memberActivity.getActivityStatus()).isEqualTo(ActivityStatus.ACTIVE);
		assertThat(memberActivity.getIsDeleted()).isFalse();
	}

	@Test
	@DisplayName("일반_구성원_활동을_생성하면_관리_항목이_함께_생성된다")
	void 일반_구성원_활동을_생성하면_관리_항목이_함께_생성된다() {
		MemberActivity memberActivity = createSemesterActivity();
		MemberSemester memberSemester = memberActivity.getMemberSemester();

		assertThat(memberSemester).isNotNull();
		assertThat(memberSemester.getSemesterId()).isEqualTo(2L);
		assertThat(memberSemester.getMemberActivity()).isSameAs(memberActivity);
	}
}