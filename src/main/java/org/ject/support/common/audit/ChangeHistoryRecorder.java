package org.ject.support.common.audit;

import lombok.RequiredArgsConstructor;
import org.ject.support.domain.changehistory.domain.ChangeHistory;
import org.ject.support.domain.changehistory.repository.ChangeHistoryRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChangeHistoryRecorder {

    private final ChangeHistoryRepository changeHistoryRepository;

    public void record(AuditContext context) {
        if (context.getDetails().isEmpty()) {
            return;
        }

        ChangeHistory changeHistory = ChangeHistory.create(
            context.getDomain(),
            context.getAction(),
            context.getTargetId(),
            context.getActorId(),
            context.getDetails()
        );

        changeHistoryRepository.save(changeHistory);
    }
}
