package org.ject.support.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.dto.SubmittedApplyBulkDeleteRequest;
import org.ject.support.admin.dto.SubmittedApplyCountResponse;
import org.ject.support.admin.dto.SubmittedApplyDetailResponse;
import org.ject.support.admin.dto.SubmittedApplyEditRequest;
import org.ject.support.admin.dto.SubmittedApplyResponse;
import org.ject.support.admin.service.SubmittedApplyService;
import org.ject.support.domain.member.JobFamily;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
@RequestMapping("/admin/submitted-applies")
@RequiredArgsConstructor
public class SubmittedApplyController implements SubmittedApplyApiSpec {

    private final SubmittedApplyService submittedApplyService;

    @Override
    @GetMapping("/count")
    public SubmittedApplyCountResponse getSubmittedApplyCount() {
        return submittedApplyService.countSubmittedApply();
    }

    @Override
    @GetMapping
    public Page<SubmittedApplyResponse> findSubmittedApplies(@RequestParam(required = false) final JobFamily jobFamily,
                                                             @RequestParam(required = false) final Long semesterId,
                                                             @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) final Pageable pageable) {
        return submittedApplyService.findSubmittedApplies(jobFamily, semesterId, pageable);
    }

    @Override
    @GetMapping("/{applyId}")
    public SubmittedApplyDetailResponse findSubmittedApplyDetail(@PathVariable("applyId") final Long applyId) {
        return submittedApplyService.findSubmittedApplyDetail(applyId);
    }

    @Override
    @PutMapping("/{applyId}")
    public void editSubmittedApply(@PathVariable("applyId") final Long applyId,
                                   @RequestBody @Valid final SubmittedApplyEditRequest request) {
        submittedApplyService.updateSubmittedApply(applyId, request);
    }

    @Override
    @DeleteMapping("/{applyId}")
    public void deleteSubmittedApply(@PathVariable("applyId") final Long applyId) {
        submittedApplyService.deleteSubmittedApply(applyId);
    }

    @Override
    @DeleteMapping
    public int deleteSubmittedApplies(@RequestBody @Valid final SubmittedApplyBulkDeleteRequest request) {
        return submittedApplyService.deleteSubmittedApplies(request.applyIds());
    }
}
