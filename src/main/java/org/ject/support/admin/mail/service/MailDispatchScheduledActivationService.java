package org.ject.support.admin.mail.service;

import java.time.Instant;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.mail.repository.MailDispatchJobRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MailDispatchScheduledActivationService {

    private final MailDispatchJobRepository jobRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int activateDueJobs(Instant now, int batchSize) {
        Objects.requireNonNull(now);
        if (batchSize < 1 || batchSize > 100) {
            throw new IllegalArgumentException("batchSize must be between 1 and 100");
        }
        int activated = 0;
        for (Long jobId : jobRepository.findDueScheduledJobIds(now, PageRequest.of(0, batchSize))) {
            var job = jobRepository.findByIdForUpdate(jobId);
            if (job.isPresent() && job.get().startScheduledProcessing(now)) {
                activated++;
            }
        }
        return activated;
    }
}
