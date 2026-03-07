package org.ject.support.admin.apply.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.ject.support.admin.apply.dto.TempApplyDetailResponse;
import org.ject.support.admin.apply.dto.TempSavedApplyCountResponse;
import org.ject.support.admin.apply.dto.TempSavedApplyResponse;
import org.ject.support.domain.member.JobFamily;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Temporary Apply", description = "임시 저장된 지원서 관리")
public interface AdminTempApplyApiSpec {

    @Operation(
            summary = "임시 저장된 지원서 상세 조회",
            description = "관리자가 지원자의 임시 저장된 지원서의 정보를 조회합니다."
    )
    TempApplyDetailResponse getTempApplyDetail(@PathVariable("tempApplyId") Long tempApplyId);

    @Operation(
            summary = "임시 저장한 지원서 수 조회",
            description = "임시 저장한 지원서 총 개수를 조회합니다.")
    TempSavedApplyCountResponse getTempSavedApplyCount();

    @Operation(
            summary = "임시 저장된 지원서 삭제",
            description = "임시 저장된 지원서를 삭제합니다.")
    void deleteTempApply(@PathVariable("tempApplyId") Long tempApplyId);

    @Operation(
            summary = "임시 저장된 지원서 목록 조회",
            description = "관리자가 임시 저장된 지원서 목록을 조회합니다."
    )
    Page<TempSavedApplyResponse> getTempApplies(
            @RequestParam(required = false) JobFamily jobFamily,
            @RequestParam(required = false) final Long semesterId,
            @PageableDefault(size = 15) final Pageable pageable
    );
}
