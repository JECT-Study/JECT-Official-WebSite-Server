package org.ject.support.domain.jectalk.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.ject.support.domain.jectalk.dto.JectalkResponse;
import org.ject.support.domain.project.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Jectalk", description = "젝톡 API")
public interface JectalkApiSpec {

    @Operation(
            summary = "젝톡 목록 조회",
            description = "젝톡 목록을 조회합니다."
    )
    Page<JectalkResponse> findJectalks(
            @PageableDefault(size = 12) Pageable pageable,
            @Parameter(description = "기수 (SEMESTER_1, SEMESTER_2, SEMESTER_3)", example = "SEMESTER_1")
            @RequestParam(required = false) Project.Category category);
}
