package org.ject.support.admin.mail.repository;

import org.ject.support.admin.mail.domain.MailDispatchTarget;
import org.ject.support.admin.mail.domain.MailDispatchTargetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MailDispatchTargetQueryRepository {

    Page<MailDispatchTarget> findTargets(Long dispatchJobId,
                                         MailDispatchTargetStatus status,
                                         Pageable pageable);
}
