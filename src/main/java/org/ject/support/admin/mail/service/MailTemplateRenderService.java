package org.ject.support.admin.mail.service;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.mail.domain.MailScenario;
import org.ject.support.admin.mail.domain.ReservedMailVariable;
import org.ject.support.admin.mail.exception.MailErrorCode;
import org.ject.support.admin.mail.exception.MailException;
import org.ject.support.domain.applicant.entity.Applicant;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.SelectionResult;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailTemplateRenderService {

    private final MailTemplateEngine mailTemplateEngine;
    private final MailTemplateValidator mailTemplateValidator;

    public void validate(MailScenario scenario,
                         String subjectTemplate,
                         String bodyTemplate,
                         Map<String, String> inputVariables) {
        mailTemplateValidator.validateSyntax(subjectTemplate);
        mailTemplateValidator.validateSyntax(bodyTemplate);
        mailTemplateValidator.validateAllowedPlaceholders(subjectTemplate, scenario.getCustomVariables());
        mailTemplateValidator.validateAllowedPlaceholders(bodyTemplate, scenario.getCustomVariables());
        mailTemplateValidator.validateInputVariables(scenario.getCustomVariables(), inputVariables);
    }

    public RenderedMail render(Apply apply,
                               String subjectTemplate,
                               String bodyTemplate,
                               Map<String, String> inputVariables) {
        Map<String, Object> variables = buildVariables(apply, inputVariables);
        String subject = render(subjectTemplate, variables, apply.getId());
        String body = render(bodyTemplate, variables, apply.getId());
        return new RenderedMail(subject, body);
    }

    private Map<String, Object> buildVariables(Apply apply, Map<String, String> inputVariables) {
        Map<String, Object> variables = new HashMap<>();
        if (inputVariables != null) {
            variables.putAll(inputVariables);
        }

        // 입력 변수를 먼저 넣고, 시스템 변수를 뒤에서 덮어쓴다.
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
        // 렌더링 후 남은 변수도 없는지 확인한다.
        mailTemplateValidator.validateResolvedTemplate(rendered, applyId);
        return rendered;
    }

    public record RenderedMail(String subject, String body) {
    }
}
