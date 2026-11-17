package org.ject.support.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record CursorPageResponse<T>(
	@Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
	List<T> content,
	@Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
	int size,
	@Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
	boolean hasNext,
	@Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
	Long nextCursor,
	@Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
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
