package org.ject.support.admin.mail.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.mail.domain.MailDispatchOutbox;
import org.ject.support.admin.mail.exception.MailErrorCode;
import org.ject.support.external.email.exception.EmailException;
import org.ject.support.external.email.service.EmailSendService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailDispatchDeliveryService {

    private final EmailSendService emailSendService;
    private final MailDispatchPersistenceService mailDispatchPersistenceService;
    private final MailDispatchRetryPolicy mailDispatchRetryPolicy;

    public void deliver(MailDispatchOutbox outbox) {
        try {
            emailSendService.sendEmail(outbox.getEmail(), outbox.getSubject(), outbox.getBody());
        } catch (Exception exception) {
            String failureReason = exception instanceof EmailException emailException
                    ? emailException.getErrorCode().getCode()
                    : MailErrorCode.MAIL_SEND_FAILURE.getCode();
            if (mailDispatchRetryPolicy.shouldRetry(failureReason, outbox.getAttemptCount())) {
                mailDispatchPersistenceService.scheduleRetry(
                        outbox.getId(),
                        failureReason,
                        mailDispatchRetryPolicy.nextAttemptAt(outbox.getAttemptCount(), LocalDateTime.now()));
                return;
            }
            mailDispatchPersistenceService.recordFailure(
                    outbox.getDispatchJob().getId(), outbox.getApplyId(), failureReason);
            return;
        }
        mailDispatchPersistenceService.recordSuccess(
                outbox.getDispatchJob().getId(), outbox.getApplyId());
    }
}
