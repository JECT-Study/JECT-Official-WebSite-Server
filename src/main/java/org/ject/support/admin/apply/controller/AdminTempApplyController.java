package org.ject.support.admin.apply.controller;

import lombok.RequiredArgsConstructor;
import org.ject.support.admin.apply.dto.TempApplyDetailResponse;
import org.ject.support.admin.apply.dto.TempSavedApplyCountResponse;
import org.ject.support.admin.apply.service.AdminTempApplyService;
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
    public TempApplyDetailResponse getTempApplyDetail(@PathVariable("tempApplyId") Long tempApplyId) {
        return adminTempApplyService.getTempApplyDetail(tempApplyId);
    }

    @GetMapping("/count")
    public TempSavedApplyCountResponse getTempSavedApplyCount() {
        return adminTempApplyService.getTempSavedApplyCount();
    }

    @DeleteMapping("/{tempApplyId}")
    public void deleteTempApply(@PathVariable final Long tempApplyId) {
        adminTempApplyService.deleteTempApply(tempApplyId);
    }

}
