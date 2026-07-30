package org.ject.support.admin.semester.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.ject.support.admin.semester.dto.EditSemesterEventsRequest;
import org.ject.support.admin.semester.dto.SemesterEventsResponse;
import org.ject.support.domain.recruit.domain.SemesterEventType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "AdminSemesterEvent", description = "기수별 행사 관리 API")
public interface AdminSemesterEventApiSpec {

    @Operation(
            summary = "기수별 행사 목록 조회",
            description = "선택한 기수와 행사 타입의 행사 목록을 조회합니다.")
    SemesterEventsResponse getEvents(
            @PathVariable Long semesterId,
            @RequestParam SemesterEventType type
    );

    @Operation(
            summary = "기수별 행사 일괄 편집",
            description = "선택한 기수의 행사 추가와 이름 수정을 원자적으로 처리합니다.")
    void editEvents(
            @PathVariable Long semesterId,
            @RequestBody @Valid EditSemesterEventsRequest request
    );
}
