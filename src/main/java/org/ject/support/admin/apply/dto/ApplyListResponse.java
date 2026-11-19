package org.ject.support.admin.apply.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record ApplyListResponse<T>(
        @Schema(description = "조회 중인 모집 공고 제목", nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String recruitTitle,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        List<T> content,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        int size,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        boolean hasNext,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        Long nextCursor,
        @Schema(description = "검색 필터를 적용한 전체 지원서 수", nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        long totalCount
) {
    public static <T> ApplyListResponse<T> of(String recruitTitle, List<T> content, int size,
                                            boolean hasNext, Long nextCursor, long totalCount) {
        return new ApplyListResponse<>(recruitTitle, content, size, hasNext, nextCursor, totalCount);
    }
}
