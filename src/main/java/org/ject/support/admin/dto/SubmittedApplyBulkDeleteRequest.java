package org.ject.support.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "지원서 일괄 삭제 요청")
public record SubmittedApplyBulkDeleteRequest(
        @Schema(description = "삭제할 지원서 ID 목록", example = "[1, 2, 3]")
        @NotEmpty List<Long> applyIds
) {}
