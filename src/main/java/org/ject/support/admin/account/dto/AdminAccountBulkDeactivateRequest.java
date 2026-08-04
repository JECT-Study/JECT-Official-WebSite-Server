package org.ject.support.admin.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "관리자 계정 일괄 비활성화 요청")
public record AdminAccountBulkDeactivateRequest(
        @Schema(description = "비활성화할 관리자 계정 ID", example = "1")
        @NotNull
        Long memberId
) {
}
