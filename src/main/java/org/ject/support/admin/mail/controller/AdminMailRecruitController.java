package org.ject.support.admin.mail.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.mail.dto.MailRecruitResponse;
import org.ject.support.admin.mail.service.MailRecruitService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/mails/recruits")
public class AdminMailRecruitController implements AdminMailRecruitApiSpec {

    private final MailRecruitService mailRecruitService;

    @Override
    @GetMapping
    public List<MailRecruitResponse> getRecruits() {
        return mailRecruitService.getRecruits();
    }
}
