package org.ject.support.admin.mail.service;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.mail.domain.MailScenario;
import org.ject.support.admin.mail.domain.MailScenarioRepository;
import org.ject.support.admin.mail.domain.ReservedMailVariable;
import org.ject.support.admin.mail.dto.MailPreviewResponse;
import org.ject.support.admin.mail.dto.PreviewMailRequest;
import org.ject.support.admin.mail.exception.MailErrorCode;
import org.ject.support.admin.mail.exception.MailException;
import org.ject.support.domain.applicant.entity.Applicant;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.apply.domain.SelectionResult;
import org.ject.support.domain.apply.exception.ApplyErrorCode;
import org.ject.support.domain.apply.exception.ApplyException;
import org.ject.support.domain.apply.repository.ApplyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 실제 발송 전 메일 제목과 본문 렌더링을 담당하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MailPreviewService {

    private final MailScenarioRepository mailScenarioRepository;
    private final ApplyRepository applyRepository;
    private final MailTemplateEngine mailTemplateEngine;
    private final MailTemplateValidator mailTemplateValidator;

    public MailPreviewResponse preview(PreviewMailRequest request) {
        MailScenario scenario = findActiveScenario(request.scenarioId());
        Apply apply = findSubmittedApply(request.applyId());

        validateTemplates(scenario);
        mailTemplateValidator.validateInputVariables(scenario.getCustomVariables(), request.inputVariables());

        Map<String, Object> variables = buildVariables(apply, request.inputVariables());
        String subject = render(scenario.getSubjectTemplate(), variables, request.applyId());
        String body = render(scenario.getBodyTemplate(), variables, request.applyId());

        return new MailPreviewResponse(
                request.scenarioId(),
                request.applyId(),
                apply.getApplicant().getEmail(),
                subject,
                body
        );
    }

    private MailScenario findActiveScenario(Long scenarioId) {
        MailScenario scenario = mailScenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new MailException(MailErrorCode.SCENARIO_NOT_FOUND));

        if (!scenario.isActive()) {
            throw new MailException(MailErrorCode.INACTIVE_SCENARIO);
        }

        return scenario;
    }

    private Apply findSubmittedApply(Long applyId) {
        Apply apply = applyRepository.findByIdAndStatusWithApplicant(applyId, ApplyStatus.SUBMITTED)
                .orElseThrow(() -> new ApplyException(ApplyErrorCode.NOT_FOUND_APPLY));

        if (apply.isNotSubmitted()) {
            throw new ApplyException(ApplyErrorCode.NOT_SUBMITTED);
        }

        return apply;
    }

    private void validateTemplates(MailScenario scenario) {
        mailTemplateValidator.validateSyntax(scenario.getSubjectTemplate());
        mailTemplateValidator.validateSyntax(scenario.getBodyTemplate());
        mailTemplateValidator.validateAllowedPlaceholders(
                scenario.getSubjectTemplate(), scenario.getCustomVariables());
        mailTemplateValidator.validateAllowedPlaceholders(
                scenario.getBodyTemplate(), scenario.getCustomVariables());
    }

    private Map<String, Object> buildVariables(Apply apply, Map<String, String> inputVariables) {
        Map<String, Object> variables = new HashMap<>();
        variables.putAll(inputVariables);

        Applicant applicant = apply.getApplicant();
        addVariable(variables, ReservedMailVariable.name.name(), applicant.getName());
        if (apply.getRecruit() != null && apply.getRecruit().getSemester() != null) {
            addVariable(variables, ReservedMailVariable.semester.name(), apply.getRecruit().getSemester().getName());
        }

        if (SelectionResult.WAITLISTED.equals(apply.getSelectionResult())) {
            addVariable(variables, ReservedMailVariable.waitlistNumber.name(),
                    apply.getWaitlistNumber() == null ? null : apply.getWaitlistNumber().toString());
        }

        return variables;
    }

    private void addVariable(Map<String, Object> variables, String key, String value) {
        if (value != null) {
            variables.put(key, value);
        }
    }

    private String render(String template, Map<String, Object> variables, Long applyId) {
        String rendered = mailTemplateEngine.render(template, variables);
        mailTemplateValidator.validateResolvedTemplate(rendered, applyId);
        return rendered;
    }
}
