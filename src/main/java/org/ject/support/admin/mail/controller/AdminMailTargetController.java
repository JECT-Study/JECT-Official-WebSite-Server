package org.ject.support.admin.mail.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.mail.dto.MailTargetResponse;
import org.ject.support.admin.mail.dto.MailTargetSelectionResult;
import org.ject.support.admin.mail.service.MailTargetService;
import org.ject.support.domain.apply.domain.SelectionResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/mails/targets")
@Tag(name = "AdminMailTarget", description = "메일 발송 대상자 조회 API (어드민 전용)")
public class AdminMailTargetController {

    private final MailTargetService mailTargetService;

    @GetMapping
    @Operation(summary = "메일 발송 대상자 조회", description = "모집 공고와 선정 결과로 메일 발송 대상자를 조회합니다.")
    public List<MailTargetResponse> getTargets(
            @RequestParam("recruitId") Long recruitId,
            @RequestParam(value = "selectionResult", required = false) MailTargetSelectionResult selectionResult) {
        SelectionResult result = selectionResult == null ? null : selectionResult.toSelectionResult();
        return mailTargetService.getTargets(recruitId, result);
    }
}
