package org.ject.support.admin.mail.repository;

import java.util.List;
import java.util.Optional;
import org.ject.support.admin.mail.domain.MailDispatchOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MailDispatchOutboxRepository extends JpaRepository<MailDispatchOutbox, Long> {

    Optional<MailDispatchOutbox> findByDispatchJobIdAndApplyId(Long dispatchJobId, Long applyId);

    List<MailDispatchOutbox> findAllByDispatchJobIdOrderByIdAsc(Long dispatchJobId);
}
