package org.ject.support.admin.mail.repository;

import org.ject.support.admin.mail.domain.MailDispatchJob;
import org.ject.support.admin.mail.dto.MailDispatchJobSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MailDispatchJobQueryRepository {

    Page<MailDispatchJob> findJobs(Long requestedByAdminId,
                                   MailDispatchJobSearchCondition condition,
                                   Pageable pageable);
}
