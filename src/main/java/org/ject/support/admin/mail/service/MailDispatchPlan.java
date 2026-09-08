package org.ject.support.admin.mail.service;

import java.util.List;
import java.util.Map;

public record MailDispatchPlan(
        Long scenarioId,
        Long recruitId,
        Long requestedByAdminId,
        String subjectTemplate,
        String bodyTemplate,
        Map<String, String> inputVariables,
        List<Target> targets
) {

    public record Target(Long applyId, String email, String subject, String body) {
    }
}
