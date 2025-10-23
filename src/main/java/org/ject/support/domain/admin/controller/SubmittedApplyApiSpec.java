package org.ject.support.domain.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.ject.support.domain.admin.dto.SubmittedApplyBulkDeleteRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Submitted Apply", description = "제출된 지원서 관리 API")
public interface SubmittedApplyApiSpec {

    @Operation(
            summary = "제출된 지원서 삭제",
            description = "선택한 제출된 지원서를 삭제합니다.")
    void deleteSubmittedApply(@PathVariable final Long applyId);

    @Operation(
            summary = "제출된 지원서 다수 삭제",
            description = "선택한 다수의 제출된 지원서들을 삭제합니다.")
    void deleteSubmittedApplies(@RequestBody @Valid final SubmittedApplyBulkDeleteRequest request);
}
