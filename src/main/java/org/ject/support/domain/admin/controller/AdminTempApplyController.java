package org.ject.support.domain.admin.controller;

import lombok.RequiredArgsConstructor;
import org.ject.support.domain.admin.dto.TempSavedApplyCountResponse;
import org.ject.support.domain.admin.service.AdminTempApplyService;
import org.ject.support.domain.apply.dto.TempApplyDetailResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/temp/apply")
public class AdminTempApplyController implements AdminTempApplyApiSpec {

    private final AdminTempApplyService adminTempApplyService;

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
}
