package org.ject.support.external.email.controller;

import lombok.RequiredArgsConstructor;
import org.ject.support.external.email.dto.SendManualBulkTemplatedEmailRequest;
import org.ject.support.external.email.dto.SendManualTemplatedEmailRequest;
import org.ject.support.external.email.service.SesEmailSendService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/emails/send/manual")
@RequiredArgsConstructor
public class ManualEmailSendController {

    private final SesEmailSendService emailSendService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void sendManualTemplatedEmail(@RequestBody SendManualTemplatedEmailRequest request) {
        emailSendService.sendTemplatedEmail(request.sendGroupCode(), request.to(), request.content());
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void sendManualBulkTemplatedEmail(@RequestBody SendManualBulkTemplatedEmailRequest request) {
        emailSendService.sendBulkTemplatedEmail(request.sendGroupCode(), request.toList(), request.content());
    }
}
