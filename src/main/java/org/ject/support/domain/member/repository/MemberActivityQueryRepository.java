package org.ject.support.domain.member.repository;

import java.util.List;

import org.ject.support.admin.member.dto.projection.SearchMemberSemesterProjection;
import org.ject.support.admin.member.dto.request.MemberSemesterSearchCondition;
import org.ject.support.domain.member.dto.TeamMemberNames;

public interface MemberActivityQueryRepository {
	List<SearchMemberSemesterProjection> searchMemberSemesters(
		MemberSemesterSearchCondition condition,
		int limit
	);

	long countMemberSemesters(MemberSemesterSearchCondition conditon);

	TeamMemberNames findMemberNamesByTeamId(Long teamId);
}
