package org.ject.support.admin.mail.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.mail.dto.MailTargetResponse;
import org.ject.support.admin.mail.dto.MailTargetSearchRequest;
import org.ject.support.admin.mail.service.MailTargetService;
import org.ject.support.domain.apply.domain.SelectionResult;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/mails/targets")
public class AdminMailTargetController implements AdminMailTargetApiSpec {

    private final MailTargetService mailTargetService;

    @Override
    @GetMapping
    public List<MailTargetResponse> searchTargets(
            @ParameterObject @ModelAttribute @Valid MailTargetSearchRequest request) {
        SelectionResult result = request.selectionResult() == null
                ? null
                : request.selectionResult().toSelectionResult();
        return mailTargetService.searchTargets(request.recruitId(), result);
    }
}
