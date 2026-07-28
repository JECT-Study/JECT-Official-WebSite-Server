package org.ject.support.admin.member.dto.result;

import java.util.List;
import java.util.function.Function;

import org.ject.support.common.response.CursorPageResponse;

public record MemberPageResult<T>(
	List<T> content,
	long totalCount
) {
	public static <T> MemberPageResult<T> of(List<T> content, long totalCount) {
		return new MemberPageResult<>(content, totalCount);
	}

	// size + 1 조회 결과를 커서 응답으로 변환
	public <R> CursorPageResponse<R> toCursorPageResponse(
		int size,
		Function<T, R> responseMapper,
		Function<T, Long> cursorExtractor
	) {
		boolean hasNext = content.size() > size;
		List<T> pageContent = hasNext
			? content.subList(0, size)
			: content;

		List<R> responses = pageContent.stream()
			.map(responseMapper)
			.toList();

		Long nextCursor = hasNext && !pageContent.isEmpty()
			? cursorExtractor.apply(pageContent.get(pageContent.size() - 1))
			: null;

		return CursorPageResponse.of(
			responses,
			size,
			hasNext,
			nextCursor,
			totalCount
		);
	}
}
