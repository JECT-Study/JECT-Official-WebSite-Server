package org.ject.support.domain.recruit.controller;

import lombok.RequiredArgsConstructor;
import org.ject.support.common.security.AuthPrincipal;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.dto.ApplyTemporaryRequest;
import org.ject.support.domain.recruit.dto.ApplyTemporaryResponse;
import org.ject.support.domain.recruit.dto.SubmitApplicationRequest;
import org.ject.support.domain.recruit.service.ApplyUsecase;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/apply")
@RequiredArgsConstructor
public class ApplyController {
    private final ApplyUsecase applyUsecase;

    @GetMapping("/temp")
    @PreAuthorize("hasRole('ROLE_TEMP')")
    public ApplyTemporaryResponse getTemporaryApplication(@AuthPrincipal Long memberId) {
        return applyUsecase.getTemporaryApplication(memberId);
    }

    @PostMapping("/temp")
    @PreAuthorize("hasRole('ROLE_TEMP')")
    public void applyTemporary(@AuthPrincipal Long memberId,
                               @RequestParam JobFamily jobFamily,
                               @RequestBody ApplyTemporaryRequest request) {
        applyUsecase.applyTemporary(jobFamily, memberId, request.answers(), request.portfolios());
    }

    @PutMapping("/job")
    @PreAuthorize("hasRole('ROLE_TEMP')")
    public void changeJobFamily(@AuthPrincipal Long memberId, @RequestBody JobFamily newJobFamily) {
        applyUsecase.changeJobFamily(memberId, newJobFamily);
    }

    @PostMapping("/submit")
    @PreAuthorize("hasRole('ROLE_TEMP')")
    public void submitApplication(@AuthPrincipal Long memberId,
                                  @RequestParam JobFamily jobFamily,
                                  @RequestBody SubmitApplicationRequest request) {
        applyUsecase.submitApplication(memberId, jobFamily, request.answers(), request.portfolios());
    }
}
