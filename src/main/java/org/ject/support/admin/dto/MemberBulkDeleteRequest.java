package org.ject.support.admin.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 일괄 삭제 요청")
public record MemberBulkDeleteRequest(
        @Schema(description = "삭제할 회원 ID 목록", example = "[1, 2, 3]")
        @NotEmpty List<Long> memberIds
) {}