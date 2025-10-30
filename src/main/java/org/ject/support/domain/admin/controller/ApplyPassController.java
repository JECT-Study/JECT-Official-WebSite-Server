package org.ject.support.domain.admin.controller;

import lombok.RequiredArgsConstructor;
import org.ject.support.domain.admin.dto.ApplyPassRequest;
import org.ject.support.domain.admin.service.ApplyPassService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/apply/pass")
public class ApplyPassController implements ApplyPassApiSpec {

    private final ApplyPassService applyPassService;

    @Override
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public int passApply(@RequestBody ApplyPassRequest request) {
        return applyPassService.passApply(request.applyIds());
    }
}
