package org.ject.support.admin.mail.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.mail.domain.MailScenarioCategory;
import org.ject.support.admin.mail.domain.MailScenarioType;
import org.ject.support.admin.mail.dto.MailScenarioRequest;
import org.ject.support.admin.mail.dto.MailScenarioResponse;
import org.ject.support.admin.mail.dto.MailScenarioVariableResponse;
import org.ject.support.admin.mail.service.MailScenarioService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/mails/scenarios")
@Tag(name = "AdminMailScenario", description = "메일 발송 시나리오 관리 API (어드민 전용)")
public class AdminMailScenarioController {

    private final MailScenarioService mailScenarioService;

    @Operation(summary = "메일 템플릿 목록 조회", description = "메일 템플릿을 구분과 타입으로 필터링하여 최신순으로 조회합니다.")
    @GetMapping
    public Page<MailScenarioResponse> getScenarios(
            @RequestParam(required = false) final MailScenarioCategory category,
            @RequestParam(required = false) final MailScenarioType type,
            @PageableDefault(size = 10, sort = "createdAt", direction = Direction.DESC) final Pageable pageable) {
        return mailScenarioService.getScenarios(category, type, pageable);
    }

    @Operation(summary = "시나리오 생성", description = "새로운 메일 발송 시나리오를 생성합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MailScenarioResponse createScenario(@RequestBody @Valid MailScenarioRequest request) {
        return mailScenarioService.createScenario(request);
    }

    @Operation(summary = "시나리오 수정", description = "기존 메일 발송 시나리오를 수정합니다.")
    @PutMapping("/{scenarioId}")
    public MailScenarioResponse updateScenario(@PathVariable Long scenarioId,
                                               @RequestBody @Valid MailScenarioRequest request) {
        return mailScenarioService.updateScenario(scenarioId, request);
    }

    @Operation(summary = "시나리오 삭제", description = "메일 발송 시나리오를 삭제합니다.")
    @DeleteMapping("/{scenarioId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteScenario(@PathVariable Long scenarioId) {
        mailScenarioService.deleteScenario(scenarioId);
    }

    @Operation(summary = "시나리오 변수 목록 조회", description = "시나리오 ID로 템플릿 변수 목록(공통/개인)을 조회합니다.")
    @GetMapping("/{scenarioId}/variables")
    public MailScenarioVariableResponse getVariablesByScenario(@PathVariable Long scenarioId) {
        return mailScenarioService.getScenarioVariables(scenarioId);
    }
}
