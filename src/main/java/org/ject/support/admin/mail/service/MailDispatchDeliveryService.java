package org.ject.support.admin.mail.service;

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

    public void deliver(MailDispatchOutbox outbox) {
        try {
            emailSendService.sendEmail(outbox.getEmail(), outbox.getSubject(), outbox.getBody());
        } catch (Exception exception) {
            String failureReason = exception instanceof EmailException emailException
                    ? emailException.getErrorCode().getCode()
                    : MailErrorCode.MAIL_SEND_FAILURE.getCode();
            mailDispatchPersistenceService.recordFailure(
                    outbox.getDispatchJob().getId(), outbox.getApplyId(), failureReason);
            return;
        }
        mailDispatchPersistenceService.recordSuccess(
                outbox.getDispatchJob().getId(), outbox.getApplyId());
    }
}
