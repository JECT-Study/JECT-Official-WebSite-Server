package org.ject.support.admin.apply.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.apply.dto.AdminApplyResponse;
import org.ject.support.admin.apply.dto.SubmittedApplyBulkDeleteRequest;
import org.ject.support.admin.apply.dto.SubmittedApplyCountResponse;
import org.ject.support.admin.apply.dto.SubmittedApplyDetailResponse;
import org.ject.support.admin.apply.dto.SubmittedApplyEditRequest;
import org.ject.support.admin.apply.service.SubmittedApplyService;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.RecruitType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/submitted-applies")
@Tag(name = "Submitted Apply", description = "[관리자] 제출된 지원서 관리 API")
public class SubmittedApplyController {

    private final SubmittedApplyService submittedApplyService;

    @GetMapping("/count")
    @Operation(
            summary = "제출된 지원서 수 조회",
            description = "제출된 상태의 지원서 총 개수를 조회합니다.")
    public SubmittedApplyCountResponse getSubmittedApplyCount() {
        return submittedApplyService.countSubmittedApply();
    }

    @GetMapping
    @Operation(
            summary = "제출된 지원서 목록 조회",
            description = "제출된 지원서들의 목록을 조회합니다.")
    public Page<AdminApplyResponse> findApplies(@RequestParam(required = false) final ApplyStatus applyStatus,
                                                @RequestParam(required = false) final Long semesterId,
                                                @RequestParam(required = false) final JobFamily jobFamily,
                                                @RequestParam(required = false) final RecruitType recruitType,
                                                @PageableDefault(size = 10, sort = "createdAt", direction = Direction.DESC) final Pageable pageable) {
        return submittedApplyService.findApplies(applyStatus, semesterId, jobFamily, recruitType, pageable);
    }

    @GetMapping("/{applyId}")
    @Operation(
            summary = "제출된 지원서 상세 조회",
            description = "전달한 ID에 해당하는 제출된 지원서의 상세 정보를 조회합니다.")
    public SubmittedApplyDetailResponse findSubmittedApplyDetail(@PathVariable("applyId") final Long applyId) {
        return submittedApplyService.findSubmittedApplyDetail(applyId);
    }

    @PutMapping("/{applyId}")
    @Operation(
            summary = "제출된 지원서 수정",
            description = "전달한 ID에 해당하는 제출된 지원서의 정보를 수정 합니다.")
    public void editSubmittedApply(@PathVariable("applyId") final Long applyId,
                                   @RequestBody @Valid final SubmittedApplyEditRequest request) {
        submittedApplyService.updateSubmittedApply(applyId, request);
    }

    @DeleteMapping("/{applyId}")
    @Operation(
            summary = "제출된 지원서 삭제",
            description = "선택한 제출된 지원서를 삭제합니다.")
    public void deleteSubmittedApply(@PathVariable("applyId") final Long applyId) {
        submittedApplyService.deleteSubmittedApply(applyId);
    }

    @DeleteMapping
    @Operation(
            summary = "제출된 지원서 다수 삭제",
            description = "선택한 다수의 제출된 지원서들을 삭제합니다. 삭제한 수를 반환합니다.")
    public int deleteSubmittedApplies(@RequestBody @Valid final SubmittedApplyBulkDeleteRequest request) {
        return submittedApplyService.deleteSubmittedApplies(request.applyIds());
    }
}
