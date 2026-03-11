package org.ject.support.admin.apply.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Temporary Apply", description = "임시 저장된 지원서 관리")
public interface AdminTempApplyApiSpec {

    @Operation(
            summary = "임시 저장된 지원서 삭제",
            description = "임시 저장된 지원서를 삭제합니다.")
    void deleteTempApply(@PathVariable("tempApplyId") Long tempApplyId);

}
