package org.ject.support.domain.member.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ject.support.domain.member.fixture.MemberFixture.member;

import java.util.List;

import org.ject.support.domain.member.Region;
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

}
