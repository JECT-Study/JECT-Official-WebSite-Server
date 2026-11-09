package org.ject.support.common.audit;

import lombok.Getter;
import org.ject.support.domain.changehistory.domain.ChangeActionType;
import org.ject.support.domain.changehistory.domain.ChangeDetail;

import java.util.ArrayList;
import java.util.List;

@Getter
public class AuditContext {

    private final String domain;
    private final ChangeActionType action;
    private final String targetId;
    private final Long actorId;
    private final List<ChangeDetail> details = new ArrayList<>();

    public AuditContext(String domain, ChangeActionType action, String targetId, Long actorId) {
        this.domain = domain;
        this.action = action;
        this.targetId = targetId;
        this.actorId = actorId;
    }

    public void addDetail(ChangeDetail changeDetail) {
        this.details.add(changeDetail);
    }
}
