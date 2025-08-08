package org.ject.support.external.email.controller;

import lombok.RequiredArgsConstructor;
import org.ject.support.external.email.service.EmailAuthService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
public class EmailAuthController implements EmailAuthApi {

    private final EmailAuthService emailAuthService;

    @Override
    @PostMapping("/send/auth")
    @PreAuthorize("permitAll()")
    public void sendAuthEmail(@RequestParam String sendGroupCode, @RequestParam String email) {
        emailAuthService.sendAuthCode(sendGroupCode, email);
    }
}
