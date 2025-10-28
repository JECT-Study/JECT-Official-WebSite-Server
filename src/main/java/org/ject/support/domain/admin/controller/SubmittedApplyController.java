package org.ject.support.domain.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ject.support.domain.admin.dto.SubmittedApplyBulkDeleteRequest;
import org.ject.support.domain.admin.dto.SubmittedApplyCountResponse;
import org.ject.support.domain.admin.dto.SubmittedApplyDetailResponse;
import org.ject.support.domain.admin.dto.SubmittedApplyEditRequest;
import org.ject.support.domain.admin.dto.SubmittedApplyResponse;
import org.ject.support.domain.admin.service.SubmittedApplyService;
import org.ject.support.domain.member.JobFamily;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/submitted-applies")
@RequiredArgsConstructor
public class SubmittedApplyController implements SubmittedApplyApiSpec {

    private final SubmittedApplyService submittedApplyService;

    @Override
    @GetMapping("/count")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public SubmittedApplyCountResponse getSubmittedApplyCount() {
        return submittedApplyService.countSubmittedApply();
    }

    @Override
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Page<SubmittedApplyResponse> findSubmittedApplies(@RequestParam(required = false) final JobFamily jobFamily,
                                                             @PageableDefault(size = 15) final Pageable pageable) {
        return submittedApplyService.findSubmittedApplies(jobFamily, pageable);
    }

    @Override
    @GetMapping("/{applyId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public SubmittedApplyDetailResponse findSubmittedApplyDetail(@PathVariable("applyId") final Long applyId) {
        return submittedApplyService.findSubmittedApplyDetail(applyId);
    }

    @Override
    @PutMapping("{applyId}")
    public void editSubmittedApply(@PathVariable("applyId") final Long applyId,
                                   @RequestBody @Valid final SubmittedApplyEditRequest request) {
        submittedApplyService.updateSubmittedApply(applyId, request);
    }

    @Override
    @DeleteMapping("/{applyId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteSubmittedApply(@PathVariable("applyId") final Long applyId) {
        submittedApplyService.deleteSubmittedApply(applyId);
    }

    @Override
    @DeleteMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public int deleteSubmittedApplies(@RequestBody @Valid final SubmittedApplyBulkDeleteRequest request) {
        return submittedApplyService.deleteSubmittedApplies(request.applyIds());
    }
}
