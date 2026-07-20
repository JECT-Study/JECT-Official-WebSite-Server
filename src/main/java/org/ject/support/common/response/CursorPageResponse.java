package org.ject.support.common.response;

import java.util.List;

public record CursorPageResponse<T>(
	List<T> content,
	int size,
	boolean hasNext,
	Long nextCursor,
	long totalCount
) {
	public static <T> CursorPageResponse<T> of(
		List<T> content,
		int size,
		boolean hasNext,
		Long nextCursor,
		long totalCount
	) {
		return new CursorPageResponse<>(content, size, hasNext, nextCursor, totalCount);
	}
}
