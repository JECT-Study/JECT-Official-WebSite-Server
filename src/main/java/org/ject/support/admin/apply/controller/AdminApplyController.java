package org.ject.support.admin.apply.controller;

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
public class AdminApplyController implements AdminApplyApiSpec {

    private final AdminApplyService adminApplyService;

    @Override
    @GetMapping
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

    @Override
    @GetMapping("/{applyId}")
    public AdminApplyDetailResponse findApply(@PathVariable final Long applyId,
                                              @RequestParam(required = false) final ApplyStatus applyStatus) {
        return adminApplyService.findApply(applyId, applyStatus);
    }

    @Override
    @PutMapping("/{applyId}")
    public void editSubmittedApply(@PathVariable("applyId") final Long applyId,
                                   @RequestBody @Valid final SubmittedApplyEditRequest request) {
        adminApplyService.updateSubmittedApply(applyId, request);
    }

    @Override
    @PatchMapping("/selection-results")
    public int updateSelectionResults(@RequestBody @Valid final SelectionResultUpdateRequest request) {
        return adminApplyService.updateSelectionResults(request);
    }

    @Override
    @DeleteMapping("/{applyId}")
    public void deleteApply(@PathVariable final Long applyId) {
        adminApplyService.deleteApply(applyId);
    }

    @Override
    @DeleteMapping
    public int deleteSubmittedApplies(@RequestBody @Valid final SubmittedApplyBulkDeleteRequest request) {
        return adminApplyService.deleteApplies(request.applyIds());
    }
}
