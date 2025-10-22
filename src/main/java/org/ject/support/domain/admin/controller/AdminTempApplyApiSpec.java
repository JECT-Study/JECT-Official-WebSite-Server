package org.ject.support.domain.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.ject.support.domain.apply.dto.TempApplyDetailResponse;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Admin", description = "임시 저장된 지원서 관리")
public interface AdminTempApplyApiSpec {

    @Operation(
            summary = "임시 저장된 지원서 상세 조회",
            description = "관리자가 지원자의 임시 저장된 지원서의 정보를 조회합니다."
    )
    TempApplyDetailResponse getTempApplyDetail(@PathVariable("tempApplyId") Long tempApplyId);
}
