package org.ject.support.admin.member.dto.result;

import java.util.List;

public record MemberPageResult<T>(
	List<T> content,
	long totalCount
) {
	public static <T> MemberPageResult<T> of(List<T> content, long totalCount) {
		return new MemberPageResult<>(content, totalCount);
	}
}
