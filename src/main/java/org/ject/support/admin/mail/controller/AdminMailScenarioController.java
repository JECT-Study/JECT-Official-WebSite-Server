package org.ject.support.admin.mail.controller;

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
public class AdminMailScenarioController implements AdminMailScenarioApiSpec {

    private final MailScenarioService mailScenarioService;

    @Override
    @GetMapping
    public Page<MailScenarioResponse> searchScenarios(
            @RequestParam(required = false) final MailScenarioCategory category,
            @RequestParam(required = false) final MailScenarioType type,
            final Pageable pageable) {
        return mailScenarioService.searchScenarios(category, type, pageable);
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MailScenarioResponse createScenario(@RequestBody @Valid MailScenarioRequest request) {
        return mailScenarioService.createScenario(request);
    }

    @Override
    @PutMapping("/{scenarioId}")
    public MailScenarioResponse updateScenario(@PathVariable Long scenarioId,
                                               @RequestBody @Valid MailScenarioRequest request) {
        return mailScenarioService.updateScenario(scenarioId, request);
    }

    @Override
    @DeleteMapping("/{scenarioId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteScenario(@PathVariable Long scenarioId) {
        mailScenarioService.deleteScenario(scenarioId);
    }

    @Override
    @GetMapping("/{scenarioId}/variables")
    public MailScenarioVariableResponse getVariablesByScenario(@PathVariable Long scenarioId) {
        return mailScenarioService.getScenarioVariables(scenarioId);
    }
}
