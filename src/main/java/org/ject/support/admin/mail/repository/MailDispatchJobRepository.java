package org.ject.support.admin.mail.repository;

import java.util.Optional;
import org.ject.support.admin.mail.domain.MailDispatchJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MailDispatchJobRepository extends JpaRepository<MailDispatchJob, Long>, MailDispatchJobQueryRepository {

    Optional<MailDispatchJob> findByRequestedByAdminIdAndIdempotencyKey(
            Long requestedByAdminId, String idempotencyKey);

    Optional<MailDispatchJob> findByIdAndRequestedByAdminId(Long dispatchJobId, Long requestedByAdminId);
}
