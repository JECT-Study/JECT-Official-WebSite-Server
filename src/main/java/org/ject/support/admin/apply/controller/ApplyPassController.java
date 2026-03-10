package org.ject.support.admin.apply.controller;

import lombok.RequiredArgsConstructor;
import org.ject.support.admin.apply.dto.ApplyPassRequest;
import org.ject.support.admin.apply.service.ApplyPassService;
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
    public int passApply(@RequestBody ApplyPassRequest request) {
        return applyPassService.passApply(request.applyIds());
    }
}
