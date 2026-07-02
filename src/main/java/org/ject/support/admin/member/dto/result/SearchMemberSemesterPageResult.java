package org.ject.support.admin.member.dto.result;

import java.util.List;

import org.ject.support.admin.member.dto.projection.SearchMemberSemesterProjection;

public record SearchMemberSemesterPageResult(
	List<SearchMemberSemesterProjection> content,
	long totalCount
) {
}
