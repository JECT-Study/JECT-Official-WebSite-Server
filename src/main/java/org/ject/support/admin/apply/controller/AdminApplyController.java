package org.ject.support.admin.apply.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.apply.dto.AdminApplyDetailResponse;
import org.ject.support.admin.apply.dto.AdminApplyResponse;
import org.ject.support.admin.apply.dto.AdminApplySearchCondition;
import org.ject.support.admin.apply.dto.SelectionResultUpdateRequest;
import org.ject.support.admin.apply.dto.SubmittedApplyBulkDeleteRequest;
import org.ject.support.admin.apply.dto.SubmittedApplyEditRequest;
import org.ject.support.admin.apply.service.AdminApplyService;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.RecruitType;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/applies")
@Tag(name = "Admin Apply", description = "[관리자] 지원서 관리 API")
public class AdminApplyController {

    private final AdminApplyService adminApplyService;

    @GetMapping
    @Operation(
            summary = "지원서 목록 조회",
            description = "지원서들의 목록을 조회합니다. 모집 공고, 모집 유형, 모집 사유, 기수, 직군, 지원 상태로 필터링할 수 있습니다.")
    public Page<AdminApplyResponse> findApplies(@RequestParam(required = false) final ApplyStatus applyStatus,
                                                @RequestParam(required = false) final Long semesterId,
                                                @RequestParam(required = false) final JobFamily jobFamily,
                                                @RequestParam(required = false) final RecruitType recruitType,
                                                @RequestParam(required = false) final RecruitTypeDetail recruitTypeDetail,
                                                @RequestParam(required = false) final Long recruitId,
                                                @PageableDefault(sort = "createdAt", direction = Direction.DESC) final Pageable pageable) {
        AdminApplySearchCondition condition = new AdminApplySearchCondition(
                applyStatus, semesterId, jobFamily, recruitType, recruitTypeDetail, recruitId);
        return adminApplyService.findApplies(condition, pageable);
    }

    @GetMapping("/{applyId}")
    @Operation(
            summary = "지원서 상세 조회",
            description = "전달한 ID에 해당하는 지원서의 상세 정보를 조회합니다.")
    public AdminApplyDetailResponse findApply(@PathVariable final Long applyId,
                                              @RequestParam(required = false) final ApplyStatus applyStatus) {
        return adminApplyService.findApply(applyId, applyStatus);
    }

    @PutMapping("/{applyId}")
    @Operation(
            summary = "제출된 지원서 수정",
            description = "전달한 ID에 해당하는 제출된 지원서의 정보를 수정 합니다.")
    public void editSubmittedApply(@PathVariable("applyId") final Long applyId,
                                   @RequestBody @Valid final SubmittedApplyEditRequest request) {
        adminApplyService.updateSubmittedApply(applyId, request);
    }

    @PatchMapping("/selection-results")
    @Operation(
            summary = "지원자 선정 결과 일괄 변경",
            description = "제출 완료된 지원자들의 선정 결과와 예비 번호를 일괄 변경합니다.")
    public int updateSelectionResults(@RequestBody @Valid final SelectionResultUpdateRequest request) {
        return adminApplyService.updateSelectionResults(request);
    }

    @DeleteMapping("/{applyId}")
    @Operation(
            summary = "지원서 삭제",
            description = "전달한 ID에 해당하는 지원서를 삭제합니다.")
    public void deleteApply(@PathVariable final Long applyId) {
        adminApplyService.deleteApply(applyId);
    }

    @DeleteMapping
    @Operation(
            summary = "지원서 다수 삭제",
            description = "선택한 다수의 지원서들을 삭제합니다. 삭제한 수를 반환합니다.")
    public int deleteSubmittedApplies(@RequestBody @Valid final SubmittedApplyBulkDeleteRequest request) {
        return adminApplyService.deleteApplies(request.applyIds());
    }
}
