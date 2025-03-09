package org.ject.support.external.email;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailAuthService authEmailService;

    @PostMapping("/send/auth")
    @PreAuthorize("permitAll()")
    public void sendAuthEmail(@RequestParam String email) {
        authEmailService.sendAuthCode(email);
    }
}
