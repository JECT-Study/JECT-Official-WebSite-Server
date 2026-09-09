package org.ject.support.admin.mail.repository;

import java.util.Optional;
import org.ject.support.admin.mail.domain.MailDispatchJob;
import org.ject.support.admin.mail.domain.MailDispatchJobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MailDispatchJobRepository extends JpaRepository<MailDispatchJob, Long> {

    Optional<MailDispatchJob> findByRequestedByAdminIdAndIdempotencyKey(
            Long requestedByAdminId, String idempotencyKey);

    Page<MailDispatchJob> findAllByRequestedByAdminIdOrderByRequestedAtDescIdDesc(
            Long requestedByAdminId, Pageable pageable);

    Page<MailDispatchJob> findAllByRequestedByAdminIdAndRecruitIdOrderByRequestedAtDescIdDesc(
            Long requestedByAdminId, Long recruitId, Pageable pageable);

    Page<MailDispatchJob> findAllByRequestedByAdminIdAndStatusOrderByRequestedAtDescIdDesc(
            Long requestedByAdminId, MailDispatchJobStatus status, Pageable pageable);

    Page<MailDispatchJob> findAllByRequestedByAdminIdAndRecruitIdAndStatusOrderByRequestedAtDescIdDesc(
            Long requestedByAdminId, Long recruitId, MailDispatchJobStatus status, Pageable pageable);

    Optional<MailDispatchJob> findByIdAndRequestedByAdminId(Long dispatchJobId, Long requestedByAdminId);
}
