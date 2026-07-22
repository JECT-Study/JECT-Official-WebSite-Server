package org.ject.support.domain.member;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CareerLevel {
	UNDER_1_YEAR("1년 미만"),
	JUNIOR("주니어(1-3년차)"),
	MIDDLE("미들(4-7년차)"),
	SENIOR("시니어(8년차~)");

	private final String description;
}
