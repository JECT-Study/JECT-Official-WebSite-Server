package org.ject.support.external.email.controller;

import lombok.RequiredArgsConstructor;
import org.ject.support.external.email.dto.SendPlainTextEmailRequest;
import org.ject.support.external.email.service.EmailSendService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/emails/send")
@RequiredArgsConstructor
public class EmailSendController {

    private final EmailSendService emailSendService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void sendEmail(@RequestBody SendPlainTextEmailRequest request) {
        emailSendService.sendPlainTextEmail(request.to(), request.subject(), request.content());
    }
}
