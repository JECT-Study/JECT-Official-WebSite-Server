package org.ject.support.admin.member.dto.result;

import java.util.List;

import org.ject.support.admin.member.dto.projection.MemberMakersListProjection;

public record MemberMakersListPageResult(
	List<MemberMakersListProjection> content,
	long totalCount
) {
}
