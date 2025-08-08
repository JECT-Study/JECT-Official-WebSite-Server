package org.ject.support.domain.ministudy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.ject.support.common.springdoc.CustomApiResponse;
import org.ject.support.domain.ministudy.dto.MiniStudyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

@Tag(name = "MiniStudy", description = "미니스터디 API")
public interface MiniStudyApi {

    @Operation(
            summary = "미니스터디 목록 조회",
            description = "미니스터디 목록을 조회합니다.")
    @CustomApiResponse
    Page<MiniStudyResponse> findMiniStudies(@PageableDefault(size = 12) Pageable pageable);
}
