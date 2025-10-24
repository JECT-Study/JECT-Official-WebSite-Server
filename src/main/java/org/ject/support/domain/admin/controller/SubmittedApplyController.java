package org.ject.support.domain.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ject.support.domain.admin.dto.SubmittedApplyBulkDeleteRequest;
import org.ject.support.domain.admin.service.SubmittedApplyService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/submitted-applies")
@RequiredArgsConstructor
public class SubmittedApplyController implements SubmittedApplyApiSpec {

    private final SubmittedApplyService submittedApplyService;

    @Override
    @DeleteMapping("/{applyId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteSubmittedApply(@PathVariable final Long applyId) {
        submittedApplyService.deleteSubmittedApply(applyId);
    }

    @Override
    @DeleteMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteSubmittedApplies(@RequestBody @Valid final SubmittedApplyBulkDeleteRequest request) {
        submittedApplyService.deleteSubmittedApplies(request.applyIds());
    }
}
