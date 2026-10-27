package org.ject.support.admin.apply.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.ject.support.admin.apply.dto.ApplyPassRequest;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Apply Pass", description = "지원자 합격 API")
public interface ApplyPassApiSpec {

    @Operation(
            summary = "지원자 합격",
            description = "지원서의 선정 결과를 합격으로 지정합니다."
    )
    void passApply(@RequestBody ApplyPassRequest request);
}
