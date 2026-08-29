package org.ject.support.domain.member.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ject.support.domain.member.fixture.MemberFixture.member;

import java.util.List;

import org.ject.support.domain.member.Region;
import org.ject.support.domain.member.command.EditMemberCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberTest {

	@Test
	@DisplayName("구성원을 생성한다")
	void 구성원을_생성한다() {
		// given
		List<String> interestedDomains = List.of("HEALTHCARE", "FINTECH", "AI");

		// when
	    Member member = member()
			.email("admin@ject.kr")
			.interestedDomains(interestedDomains)
			.build();

		// then
		assertThat(member.getName()).isEqualTo("김젝트");
		assertThat(member.getEmail()).isEqualTo("admin@ject.kr");
		assertThat(member.getPhoneNumber()).isEqualTo("01012345678");
		assertThat(member.getInterestedDomains()).containsExactlyElementsOf(interestedDomains);
		assertThat(member.getRegion()).isEqualTo(Region.SEOUL);
		assertThat(member.getIsDeleted()).isFalse();
	}

	@Test
	@DisplayName("입력한 기본정보만 변경되고 입력하지 않은 기본정보는 유지된다")
	void 입력한_기본정보만_변경되고_입력하지_않은_기본정보는_유지된다() {
		// given
		Member member = member().build();
		EditMemberCommand command = new EditMemberCommand("수정된이름", null, null, Region.BUSAN, null);

		// when
		member.edit(command);

		// then
		assertThat(member.getName()).isEqualTo("수정된이름");
		assertThat(member.getRegion()).isEqualTo(Region.BUSAN);
		assertThat(member.getEmail()).isEqualTo("member@test.com");
		assertThat(member.getPhoneNumber()).isEqualTo("01012345678");
		assertThat(member.getInterestedDomains()).containsExactly("커머스");
	}

	@Test
	@DisplayName("관심 도메인을 입력하면 기존 관심 도메인이 새로운 목록으로 교체된다")
	void 관심_도메인을_입력하면_기존_관심_도메인이_새로운_목록으로_교체된다() {
		// given
		Member member = member().interestedDomains(List.of("커머스")).build();
		EditMemberCommand command = new EditMemberCommand(null, null, null, null, List.of("핀테크", "헬스케어"));

		// when
		member.edit(command);

		// then
		assertThat(member.getInterestedDomains()).containsExactly("핀테크", "헬스케어");
	}

}
