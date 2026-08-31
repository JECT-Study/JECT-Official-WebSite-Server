package org.ject.support.admin.mail.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.mail.dto.MailDispatchResponse;
import org.ject.support.admin.mail.dto.SendMailDispatchRequest;
import org.ject.support.admin.mail.service.MailDispatchUseCase;
import org.ject.support.common.security.AuthPrincipal;
import org.springframework.http.HttpStatus;
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
