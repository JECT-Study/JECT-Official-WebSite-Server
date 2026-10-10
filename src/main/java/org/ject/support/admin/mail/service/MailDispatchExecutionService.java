package org.ject.support.admin.mail.service;

import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.mail.repository.MailDispatchOutboxRepository;
import org.ject.support.external.email.exception.EmailErrorCode;
import org.ject.support.external.email.exception.EmailException;
import org.ject.support.external.email.service.EmailSendService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class MailDispatchExecutionService {

    private static final Duration CLAIM_LEASE = Duration.ofMinutes(2);

    private final MailDispatchClaimService claimService;
    private final MailDispatchOutboxRepository outboxRepository;
    private final EmailSendService emailSendService;

    public void executeJob(Long dispatchJobId) {
        outboxRepository.findIdsByDispatchJobId(dispatchJobId).forEach(this::execute);
    }

    public void execute(Long outboxId) {
        var claimed = claimService.claim(outboxId, LocalDateTime.now(), CLAIM_LEASE);
        if (claimed.isEmpty()) {
            return;
        }
        var claim = claimed.get();
        // claim 커밋 뒤 저장된 snapshot으로 발송하고 외부 호출 중 DB 트랜잭션은 유지하지 않는다.
        try {
            emailSendService.sendEmail(claim.email(), claim.subject(), claim.body());
        } catch (Exception exception) {
            if (exception instanceof EmailException emailException
                    && (emailException.getErrorCode() == EmailErrorCode.EMAIL_SEND_FAILURE
                    || emailException.getErrorCode() == EmailErrorCode.TOO_MANY_EMAIL_REQUESTS)) {
                claimService.recordFailure(outboxId, claim.claimToken(), LocalDateTime.now(),
                        emailException.getErrorCode());
            } else {
                claimService.recordUnknown(outboxId, claim.claimToken());
            }
            return;
        }
        // 결과 저장 오류는 발송 실패로 바꾸거나 재발송하지 않고 호출자에게 전달한다.
        claimService.recordSuccess(outboxId, claim.claimToken(), LocalDateTime.now());
    }
}
