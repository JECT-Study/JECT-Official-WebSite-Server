package org.ject.support.domain.recruit.controller;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.ject.support.common.security.AuthPrincipal;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.service.ApplyUsecase;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/apply")
@RequiredArgsConstructor
public class ApplyController {
    private final ApplyUsecase applyUsecase;

    @PostMapping("/temp")
    public void applyTemporary(@AuthPrincipal Long memberId,
                               @RequestParam JobFamily jobFamily,
                               @RequestBody Map<String, String> answers) {
        applyUsecase.applyTemporary(jobFamily, memberId, answers);
    }
}
