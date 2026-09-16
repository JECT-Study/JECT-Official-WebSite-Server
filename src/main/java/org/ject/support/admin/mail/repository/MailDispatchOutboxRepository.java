package org.ject.support.admin.mail.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.ject.support.admin.mail.domain.MailDispatchOutbox;
import org.ject.support.admin.mail.domain.MailDispatchOutboxStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MailDispatchOutboxRepository extends JpaRepository<MailDispatchOutbox, Long> {

    Optional<MailDispatchOutbox> findByDispatchJobIdAndApplyId(Long dispatchJobId, Long applyId);

    @EntityGraph(attributePaths = "dispatchJob")
    @Query("select outbox from MailDispatchOutbox outbox where outbox.id = :id")
    Optional<MailDispatchOutbox> findByIdWithDispatchJob(@Param("id") Long id);

    List<MailDispatchOutbox> findAllByDispatchJobIdOrderByIdAsc(Long dispatchJobId);

    @Query("""
            select outbox
            from MailDispatchOutbox outbox
            where outbox.status in :statuses
            order by outbox.id asc
            """)
    List<MailDispatchOutbox> findCandidates(
            @Param("statuses") Collection<MailDispatchOutboxStatus> statuses,
            Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update MailDispatchOutbox outbox
            set outbox.status = :processing,
                outbox.claimedBy = :claimedBy,
                outbox.leaseUntil = :leaseUntil
            where outbox.id = :id
              and (
                  outbox.status = :pending
                  or (outbox.status = :processing and
                      (outbox.leaseUntil is null or outbox.leaseUntil < :now))
              )
            """)
    int claim(@Param("id") Long id,
              @Param("claimedBy") String claimedBy,
              @Param("now") LocalDateTime now,
              @Param("leaseUntil") LocalDateTime leaseUntil,
              @Param("pending") MailDispatchOutboxStatus pending,
              @Param("processing") MailDispatchOutboxStatus processing);
}
