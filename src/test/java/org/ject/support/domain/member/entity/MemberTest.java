package org.ject.support.domain.member.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.ject.support.domain.member.Region;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberTest {

	@Test
	@DisplayName("")
	void 구성원을_생성한다() {
	    Member member = Member.create(
			"김젝트",
			"admin@ject.kr",
			"01012345678",
			List.of("HEALTHCARE","FINTECH", "AI"),
			Region.SEOUL
		);

		assertEquals("김젝트", member.getName());
		assertEquals("admin@ject.kr", member.getEmail());
		assertEquals("01012345678", member.getPhoneNumber());
		assertEquals(List.of("HEALTHCARE","FINTECH", "AI"), member.getInterestedDomains());
		assertEquals(Region.SEOUL, member.getRegion());

	}

}