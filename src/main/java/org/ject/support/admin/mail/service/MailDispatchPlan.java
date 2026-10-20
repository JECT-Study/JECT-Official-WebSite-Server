package org.ject.support.admin.mail.service;

import java.util.List;
import java.util.Map;
import org.ject.support.admin.mail.domain.MailScenarioType;

public record MailDispatchPlan(
        Long scenarioId,
        Long recruitId,
        Long requestedByAdminId,
        String idempotencyKey,
        String subjectTemplate,
        String bodyTemplate,
        Map<String, String> inputVariables,
        List<Target> targets,
        MailScenarioType scenarioTypeSnapshot
) {

    public MailDispatchPlan(Long scenarioId,
                            Long recruitId,
                            Long requestedByAdminId,
                            String idempotencyKey,
                            String subjectTemplate,
                            String bodyTemplate,
                            Map<String, String> inputVariables,
                            List<Target> targets) {
        this(scenarioId, recruitId, requestedByAdminId, idempotencyKey, subjectTemplate, bodyTemplate,
                inputVariables, targets, null);
    }

    public record Target(Long applyId, String email, String subject, String body, String selectionResultSnapshot) {
        public Target(Long applyId, String email, String subject, String body) {
            this(applyId, email, subject, body, null);
        }
    }
}
