package org.ject.support.admin.apply.controller;

import lombok.RequiredArgsConstructor;
import org.ject.support.admin.apply.dto.TempApplyDetailResponse;
import org.ject.support.admin.apply.dto.TempSavedApplyCountResponse;
import org.ject.support.admin.apply.dto.TempSavedApplyResponse;
import org.ject.support.admin.apply.service.TempApplyService;
import org.ject.support.domain.member.JobFamily;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/temp/apply")
public class TempApplyController implements TempApplyApiSpec {

    private final TempApplyService adminTempApplyService;

    @GetMapping("/{tempApplyId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public TempApplyDetailResponse getTempApplyDetail(@PathVariable("tempApplyId") Long tempApplyId) {
        return adminTempApplyService.getTempApplyDetail(tempApplyId);
    }

    @GetMapping("/count")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public TempSavedApplyCountResponse getTempSavedApplyCount() {
        return adminTempApplyService.getTempSavedApplyCount();
    }

    @DeleteMapping("/{tempApplyId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteTempApply(@PathVariable final Long tempApplyId) {
        adminTempApplyService.deleteTempApply(tempApplyId);
    }

    @GetMapping()
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Page<TempSavedApplyResponse> getTempApplies(@RequestParam(required = false) JobFamily jobFamily,
                                                       @PageableDefault(size = 15) Pageable pageable) {
        return adminTempApplyService.getTempApplies(jobFamily, pageable);
    }
}
