package org.ject.support.domain.member;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MakersTeam {
	TEAM_1("메이커스 1팀"),
	TEAM_2("메이커스 2팀");

	private final String description;

}
