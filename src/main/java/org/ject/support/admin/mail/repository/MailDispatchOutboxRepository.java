package org.ject.support.admin.mail.repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.ject.support.admin.mail.domain.MailDispatchOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MailDispatchOutboxRepository extends JpaRepository<MailDispatchOutbox, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select outbox from MailDispatchOutbox outbox where outbox.id = :id")
    Optional<MailDispatchOutbox> findByIdForUpdate(@Param("id") Long id);

    @Query("select outbox.dispatchJob.id from MailDispatchOutbox outbox where outbox.id = :id")
    Optional<Long> findDispatchJobIdById(@Param("id") Long id);

    Optional<MailDispatchOutbox> findByDispatchJobIdAndApplyId(Long dispatchJobId, Long applyId);

    List<MailDispatchOutbox> findAllByDispatchJobIdOrderByIdAsc(Long dispatchJobId);
}
