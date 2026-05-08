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
    public TempApplicationFormResponse findTempApplicationForm(@AuthPrincipal Long memberId,
                                                               @RequestParam Long recruitId) {
        return applyUsecase.findTempApplicationForm(memberId, recruitId);
    }

    @Override
    @PostMapping("/temp")
    @PreAuthorize("hasRole('ROLE_APPLY')")
    public void saveApplicationTemporarily(@AuthPrincipal Long memberId,
                                           @RequestParam Long recruitId,
                                           @RequestBody ApplyTemporaryRequest request) {
        applyUsecase.saveApplicationTemporarily(memberId, recruitId, request.answers(), request.portfolios());
    }

    @Override
    @DeleteMapping("/temp")
    @PreAuthorize("hasRole('ROLE_APPLY')")
    public void deleteProfileAndTempApplicationForm(@AuthPrincipal Long memberId,
                                                    @RequestParam Long recruitId) {
        applyUsecase.deleteProfileAndTempApplicationForm(memberId, recruitId);
    }

    @Override
    @PostMapping("/submit")
    @PreAuthorize("hasRole('ROLE_APPLY')")
    public void submitApplication(@AuthPrincipal Long memberId,
                                  @RequestParam Long recruitId,
                                  @RequestBody SubmitApplicationRequest request) {
        applyUsecase.submitApplication(memberId, recruitId, request.answers(), request.portfolios());
    }

    @Override
    @GetMapping("/status")
    @PreAuthorize("hasRole('ROLE_APPLY')")
    public ApplyStatusResponse checkApplyStatus(@AuthPrincipal Long memberId,
                                                @RequestParam Long recruitId) {
        return applyUsecase.checkApplyStatus(memberId, recruitId);
    }

    @Override
    @PostMapping("/profile")
    @PreAuthorize("hasRole('ROLE_APPLY')")
    public void saveProfile(@AuthPrincipal Long memberId,
                            @RequestParam Long recruitId,
                            @RequestBody @Valid ApplyProfileRequest request
    ) {
        applyUsecase.saveProfile(memberId, recruitId, request);
    }
}
