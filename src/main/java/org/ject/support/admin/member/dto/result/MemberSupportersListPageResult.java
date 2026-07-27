package org.ject.support.admin.member.dto.result;

import java.util.List;

import org.ject.support.admin.member.dto.projection.MemberSupportersListProjection;

public record MemberSupportersListPageResult(
	List<MemberSupportersListProjection> content,
	long totalCount
) {
}
