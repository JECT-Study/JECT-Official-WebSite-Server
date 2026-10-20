package org.ject.support.admin.apply.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.apply.dto.AdminApplyDetailResponse;
import org.ject.support.admin.apply.dto.SelectionResultUpdateRequest;
import org.ject.support.admin.apply.dto.SubmittedApplyBulkDeleteRequest;
import org.ject.support.admin.apply.dto.SubmittedApplyEditRequest;
import org.ject.support.admin.apply.dto.SubmittedApplyResponse;
import org.ject.support.admin.apply.dto.SubmittedApplySearchCondition;
import org.ject.support.admin.apply.dto.TempSavedApplyResponse;
import org.ject.support.admin.apply.dto.TempSavedApplySearchCondition;
import org.ject.support.admin.apply.service.AdminApplyService;
import org.ject.support.common.response.CursorPageResponse;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
    @GetMapping("/submitted")
    public CursorPageResponse<SubmittedApplyResponse> searchSubmittedApplies(
            @Valid @ModelAttribute SubmittedApplySearchCondition condition
    ) {
        return adminApplyService.searchSubmittedApplies(condition);
    }

    @Override
    @GetMapping("/temp-saved")
    public CursorPageResponse<TempSavedApplyResponse> searchTempSavedApplies(
            @Valid @ModelAttribute TempSavedApplySearchCondition condition
    ) {
        return adminApplyService.searchTempSavedApplies(condition);
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
        return adminApplyService.deleteApplies(request);
    }
}
