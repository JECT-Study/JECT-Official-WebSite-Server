package org.ject.support.domain.apply.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ject.support.common.security.AuthPrincipal;
import org.ject.support.domain.apply.dto.ApplyProfileRequest;
import org.ject.support.domain.apply.dto.ApplyStatusResponse;
import org.ject.support.domain.apply.dto.ApplyTemporaryRequest;
import org.ject.support.domain.apply.dto.SubmitApplicationRequest;
import org.ject.support.domain.apply.dto.TempApplicationFormResponse;
import org.ject.support.domain.apply.service.ApplyUsecase;
import org.ject.support.domain.member.JobFamily;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/apply")
@RequiredArgsConstructor
public class ApplyController implements ApplyApiSpec {
    private final ApplyUsecase applyUsecase;

    @Override
    @GetMapping("/temp")
    @PreAuthorize("hasRole('ROLE_APPLY')")
    public TempApplicationFormResponse findTempApplicationForm(@AuthPrincipal Long memberId) {
        return applyUsecase.findTempApplicationForm(memberId);
    }

    @Override
    @PostMapping("/temp")
    @PreAuthorize("hasRole('ROLE_APPLY')")
    public void saveApplicationTemporarily(@AuthPrincipal Long memberId,
                                           @RequestBody ApplyTemporaryRequest request) {
        applyUsecase.saveApplicationTemporarily(memberId, request.answers(), request.portfolios());
    }

    @Override
    @DeleteMapping("/temp")
    @PreAuthorize("hasRole('ROLE_APPLY')")
    public void deleteProfileAndTempApplicationForm(@AuthPrincipal Long memberId) {
        applyUsecase.deleteProfileAndTempApplicationForm(memberId);
    }

    @Override
    @PostMapping("/submit")
    @PreAuthorize("hasRole('ROLE_APPLY')")
    public void submitApplication(@AuthPrincipal Long memberId,
                                  @RequestParam JobFamily jobFamily,
                                  @RequestBody SubmitApplicationRequest request) {
        applyUsecase.submitApplication(memberId, jobFamily, request.answers(), request.portfolios());
    }

    @Override
    @GetMapping("/status")
    @PreAuthorize("hasRole('ROLE_APPLY')")
    public ApplyStatusResponse checkApplyStatus(@AuthPrincipal Long memberId) {
        return applyUsecase.checkApplySubmit(memberId);
    }

    @PostMapping("/profile")
    @PreAuthorize("hasRole('ROLE_APPLY')")
    public void saveProfile(@AuthPrincipal Long memberId,
                            @Valid @RequestBody ApplyProfileRequest request
    ) {
        applyUsecase.saveProfile(memberId, request);
    }
}
