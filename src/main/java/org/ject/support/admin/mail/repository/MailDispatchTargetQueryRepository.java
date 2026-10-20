package org.ject.support.admin.mail.repository;

import org.ject.support.admin.mail.domain.MailDispatchTargetStatus;
import org.ject.support.admin.mail.dto.MailDispatchTargetResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MailDispatchTargetQueryRepository {

    Page<MailDispatchTargetResponse> findTargets(Long dispatchJobId,
                                                 MailDispatchTargetStatus status,
                                                 Pageable pageable);
}
