package org.ject.support.domain.member.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.ject.support.domain.member.ActivityStatus;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberSupportersTest {

	@Test
	@DisplayName("입력한 운영 서포터즈 구성원 상세정보로 변경된다")
	void 입력한_운영_서포터즈_구성원_상세정보로_변경된다() {
		// given
		MemberSupporters memberSupporters = supportersActivity().getMemberSupporters();

		// when
		memberSupporters.edit("SP-002");

		// then
		assertThat(memberSupporters.getActivityCertNumber()).isEqualTo("SP-002");
	}

	@Test
	@DisplayName("상세정보를 입력하지 않으면 기존 정보가 유지된다")
	void 상세정보를_입력하지_않으면_기존_정보가_유지된다() {
		// given
		MemberSupporters memberSupporters = supportersActivity().getMemberSupporters();

		// when
		memberSupporters.edit(null);

		// then
		assertThat(memberSupporters.getActivityCertNumber()).isEqualTo("SP-001");
	}

	private MemberActivity supportersActivity() {
		return MemberActivity.createSupportersActivity(
			1L, JobFamily.OPS, RecruitTypeDetail.REGULAR, ActivityStatus.ACTIVE,
			LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), "SP-001", "memo");
	}
}
