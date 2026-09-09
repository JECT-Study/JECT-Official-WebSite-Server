package org.ject.support.admin.mail.repository;

import java.util.List;
import java.util.Optional;
import org.ject.support.admin.mail.domain.MailDispatchTarget;
import org.ject.support.admin.mail.domain.MailDispatchTargetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MailDispatchTargetRepository extends JpaRepository<MailDispatchTarget, Long> {

    Optional<MailDispatchTarget> findByDispatchJobIdAndApplyId(Long dispatchJobId, Long applyId);

    List<MailDispatchTarget> findAllByDispatchJobIdOrderByIdAsc(Long dispatchJobId);

    Page<MailDispatchTarget> findAllByDispatchJobIdOrderByIdAsc(Long dispatchJobId, Pageable pageable);

    Page<MailDispatchTarget> findAllByDispatchJobIdAndStatusOrderByIdAsc(
            Long dispatchJobId, MailDispatchTargetStatus status, Pageable pageable);
}
