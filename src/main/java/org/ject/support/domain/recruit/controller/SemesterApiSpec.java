package org.ject.support.domain.recruit.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.ject.support.domain.recruit.dto.SemesterRegisterRequest;
import org.ject.support.domain.recruit.dto.SemesterResponses;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Semester", description = "기수 API")
public interface SemesterApiSpec {

    @Operation(
            summary = "기수 등록",
            description = "새로운 기수를 등록합니다.")
    void register(@RequestBody SemesterRegisterRequest request);

    @Operation(
            summary = "전체 기수 목록 조회",
            description = "모든 기수 목록을 조회합니다.")
    SemesterResponses getAllSemesters();
}
