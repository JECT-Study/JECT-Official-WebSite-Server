package org.ject.support.domain.member.repository;

import java.util.List;

import org.ject.support.admin.member.dto.projection.SearchMemberSemesterProjection;
import org.ject.support.admin.member.dto.request.MemberSemesterSearchCondition;

public interface MemberActivityQueryRepository {
	List<SearchMemberSemesterProjection> searchMemberSemesters(
		MemberSemesterSearchCondition condition,
		int limit
	);

	long countMemberSemesters(MemberSemesterSearchCondition conditon);
}
