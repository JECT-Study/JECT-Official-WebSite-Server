package org.ject.support.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.ject.support.admin.dto.SubmittedApplyBulkDeleteRequest;
import org.ject.support.admin.dto.SubmittedApplyCountResponse;
import org.ject.support.admin.dto.SubmittedApplyDetailResponse;
import org.ject.support.admin.dto.SubmittedApplyEditRequest;
import org.ject.support.admin.dto.SubmittedApplyResponse;
import org.ject.support.domain.member.JobFamily;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Submitted Apply", description = "제출된 지원서 관리 API")
public interface SubmittedApplyApiSpec {

    @Operation(
            summary = "제출된 지원서 수 조회",
            description = "제출된 상태의 지원서 총 개수를 조회합니다.")
    SubmittedApplyCountResponse getSubmittedApplyCount();

    @Operation(
            summary = "제출된 지원서 목록 조회",
            description = "제출된 지원서들의 목록을 조회합니다.")
    Page<SubmittedApplyResponse> findSubmittedApplies(@RequestParam(required = false) final JobFamily jobFamily,
                                                      @RequestParam(required = false) final Long semesterId,
                                                      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) final Pageable pageable);

    @Operation(
            summary = "제출된 지원서 상세 조회",
            description = "전달한 ID에 해당하는 제출된 지원서의 상세 정보를 조회합니다.")
    SubmittedApplyDetailResponse findSubmittedApplyDetail(@PathVariable("applyId") final Long applyId);

    @Operation(
            summary = "제출된 지원서 수정",
            description = "전달한 ID에 해당하는 제출된 지원서의 정보를 수정 합니다.")
    void editSubmittedApply(@PathVariable("applyId") final Long applyId,
                            @RequestBody @Valid final SubmittedApplyEditRequest request);

    @Operation(
            summary = "제출된 지원서 삭제",
            description = "선택한 제출된 지원서를 삭제합니다.")
    void deleteSubmittedApply(@PathVariable("applyId") final Long applyId);

    @Operation(
            summary = "제출된 지원서 다수 삭제",
            description = "선택한 다수의 제출된 지원서들을 삭제합니다. 삭제한 수를 반환합니다.")
    int deleteSubmittedApplies(@RequestBody @Valid final SubmittedApplyBulkDeleteRequest request);
}
