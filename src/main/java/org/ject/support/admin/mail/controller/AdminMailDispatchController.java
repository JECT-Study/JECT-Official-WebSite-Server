package org.ject.support.admin.mail.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.mail.domain.MailDispatchJobStatus;
import org.ject.support.admin.mail.domain.MailDispatchTargetStatus;
import org.ject.support.admin.mail.dto.MailDispatchJobResponse;
import org.ject.support.admin.mail.dto.MailDispatchResponse;
import org.ject.support.admin.mail.dto.MailDispatchTargetResponse;
import org.ject.support.admin.mail.dto.SendMailDispatchRequest;
import org.ject.support.admin.mail.service.MailDispatchQueryService;
import org.ject.support.admin.mail.service.MailDispatchUseCase;
import org.ject.support.common.security.AuthPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/mails/dispatches")
public class AdminMailDispatchController implements AdminMailDispatchApiSpec {

    private final MailDispatchUseCase mailDispatchUseCase;
    private final MailDispatchQueryService mailDispatchQueryService;

    @Override
    @GetMapping
    public Page<MailDispatchJobResponse> searchJobs(
            @AuthPrincipal Long requestedByAdminId,
            Long recruitId,
            MailDispatchJobStatus status,
            Pageable pageable) {
        return mailDispatchQueryService.searchJobs(requestedByAdminId, recruitId, status, pageable);
    }

    @Override
    @GetMapping("/{dispatchJobId}")
    public MailDispatchJobResponse getJob(
            @AuthPrincipal Long requestedByAdminId,
            @PathVariable Long dispatchJobId) {
        return mailDispatchQueryService.getJob(requestedByAdminId, dispatchJobId);
    }

    @Override
    @GetMapping("/{dispatchJobId}/targets")
    public Page<MailDispatchTargetResponse> searchTargets(
            @AuthPrincipal Long requestedByAdminId,
            @PathVariable Long dispatchJobId,
            MailDispatchTargetStatus status,
            Pageable pageable) {
        return mailDispatchQueryService.searchTargets(requestedByAdminId, dispatchJobId, status, pageable);
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MailDispatchResponse sendMail(
            @AuthPrincipal Long requestedByAdminId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody @Valid SendMailDispatchRequest request) {
        return mailDispatchUseCase.sendMail(request, requestedByAdminId, idempotencyKey);
    }
}
