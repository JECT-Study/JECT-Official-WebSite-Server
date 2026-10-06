package org.ject.support.admin.mail.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.admin.mail.domain.MailDispatchJob;
import org.ject.support.admin.mail.domain.MailDispatchJobStatus;
import org.ject.support.admin.mail.domain.MailDispatchOutbox;
import org.ject.support.admin.mail.domain.MailDispatchTarget;
import org.ject.support.admin.mail.dto.MailDispatchResponse;
import org.ject.support.admin.mail.exception.MailErrorCode;
import org.ject.support.admin.mail.exception.MailException;
import org.ject.support.admin.mail.repository.MailDispatchJobRepository;
import org.ject.support.admin.mail.repository.MailDispatchOutboxRepository;
import org.ject.support.admin.mail.repository.MailDispatchTargetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MailDispatchCancellationService {

    private final MailDispatchJobRepository jobRepository;
    private final MailDispatchTargetRepository targetRepository;
    private final MailDispatchOutboxRepository outboxRepository;

    @Transactional
    public MailDispatchResponse cancelMail(Long requestedByAdminId, Long dispatchJobId) {
        // claim과 같은 작업 잠금을 먼저 획득해 취소와 발송 상태 변경을 직렬화 처리
        MailDispatchJob job = jobRepository.findByIdForUpdate(dispatchJobId)
                .filter(found -> found.getRequestedByAdminId().equals(requestedByAdminId))
                .orElseThrow(() -> new MailException(MailErrorCode.DISPATCH_JOB_NOT_FOUND));
        if (job.getStatus() != MailDispatchJobStatus.CANCELLED) {
            job.cancel();
            targetRepository.findAllByDispatchJobIdOrderByIdAsc(dispatchJobId).forEach(MailDispatchTarget::cancel);
            outboxRepository.findAllByDispatchJobIdOrderByIdAsc(dispatchJobId).forEach(MailDispatchOutbox::cancel);
        }
        return MailDispatchResponse.from(job);
    }
}
