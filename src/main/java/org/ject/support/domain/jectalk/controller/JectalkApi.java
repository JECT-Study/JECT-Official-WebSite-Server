package org.ject.support.domain.jectalk.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.ject.support.common.springdoc.CustomApiResponse;
import org.ject.support.domain.jectalk.dto.JectalkResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

@Tag(name = "Jectalk", description = "젝톡 API")
public interface JectalkApi {

    @Operation(
            summary = "젝톡 목록 조회",
            description = "젝톡 목록을 조회합니다."
    )
    @CustomApiResponse
    Page<JectalkResponse> findJectalks(@PageableDefault(size = 12) Pageable pageable);
}
