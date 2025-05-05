package org.ject.support.external.email.controller;

import lombok.RequiredArgsConstructor;
import org.ject.support.domain.tempapply.service.EmailApplyRemindService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/emails/send")
@RequiredArgsConstructor
public class RemindEmailSendController {

    private final EmailApplyRemindService emailApplyRemindService;

    /**
     * 임시로 사용하는 '지원 마감일 안내 이메일 발송' API
     * TODO 스케줄링으로 변경
     */
    @PostMapping("/remind/apply")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void sendEmail() {
        emailApplyRemindService.remindToApply();
    }
}
