package org.ject.support.domain.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.ject.support.domain.admin.dto.ApplyPassRequest;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Apply Pass", description = "지원자 합격 API")
public interface ApplyPassApiSpec {

    @Operation(
            summary = "지원자 합격",
            description = "지원자를 합격 처리하여 구성원으로 승인합니다."
    )
    int passApply(@RequestBody ApplyPassRequest request);
}
