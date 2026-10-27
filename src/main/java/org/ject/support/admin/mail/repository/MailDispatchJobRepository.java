package org.ject.support.admin.mail.repository;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.ject.support.admin.mail.domain.MailDispatchJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

public interface MailDispatchJobRepository extends JpaRepository<MailDispatchJob, Long>, MailDispatchJobQueryRepository {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select job from MailDispatchJob job where job.id = :id")
    Optional<MailDispatchJob> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            select job.id from MailDispatchJob job
            where job.status = org.ject.support.admin.mail.domain.MailDispatchJobStatus.SCHEDULED
              and job.scheduledAt is not null
              and job.scheduledAt <= :now
            order by job.id asc
            """)
    List<Long> findDueScheduledJobIds(@Param("now") Instant now, Pageable pageable);

    Optional<MailDispatchJob> findByRequestedByAdminIdAndIdempotencyKey(
            Long requestedByAdminId, String idempotencyKey);

    Optional<MailDispatchJob> findByIdAndRequestedByAdminId(Long dispatchJobId, Long requestedByAdminId);
}
