package org.ject.support.admin.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "관리자 계정 활성화 상태 수정 요청")
public record AdminAccountActiveUpdateRequest(
        @Schema(description = "관리자 계정 ID", example = "1")
        Long memberId,
        @Schema(description = "관리자 계정 활성화 여부", example = "true")
        @NotNull
        Boolean active
) {

    public AdminAccountActiveUpdateRequest(final Boolean active) {
        this(null, active);
    }
}
