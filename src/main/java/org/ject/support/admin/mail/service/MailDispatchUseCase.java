package org.ject.support.admin.mail.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.mail.domain.MailDispatchJob;
import org.ject.support.admin.mail.dto.MailDispatchResponse;
import org.ject.support.admin.mail.dto.SendMailDispatchRequest;
import org.ject.support.admin.mail.exception.MailErrorCode;
import org.ject.support.admin.mail.exception.MailException;
import org.ject.support.external.email.exception.EmailException;
import org.ject.support.external.email.service.EmailSendService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailDispatchUseCase {

    private static final int MAX_IDEMPOTENCY_KEY_LENGTH = 255;

    private final MailDispatchPreparationService mailDispatchPreparationService;
    private final MailDispatchPersistenceService mailDispatchPersistenceService;
    private final EmailSendService emailSendService;
    private final MailDispatchRequestFingerprint requestFingerprint;

    public MailDispatchResponse sendMail(SendMailDispatchRequest request,
                                         Long requestedByAdminId,
                                         String idempotencyKey) {
        validateIdempotencyKey(idempotencyKey);
        String requestFingerprintValue = requestFingerprint.generate(request);
        Optional<MailDispatchResponse> existingResult =
                mailDispatchPersistenceService.findResultByIdempotencyKey(
                        requestedByAdminId, idempotencyKey);
        if (existingResult.isPresent()) {
            validateRequestFingerprint(requestedByAdminId, idempotencyKey, requestFingerprintValue);
            return existingResult.get();
        }

        MailDispatchPlan plan = mailDispatchPreparationService.prepare(
                request, requestedByAdminId, idempotencyKey);
        MailDispatchJob job;
        try {
            job = mailDispatchPersistenceService.createJob(plan, requestFingerprintValue);
        } catch (DataIntegrityViolationException exception) {
            Optional<MailDispatchResponse> concurrentResult =
                    mailDispatchPersistenceService.findResultByIdempotencyKey(
                            requestedByAdminId, idempotencyKey);
            if (concurrentResult.isEmpty()) {
                throw exception;
            }
            validateRequestFingerprint(requestedByAdminId, idempotencyKey, requestFingerprintValue);
            return concurrentResult.get();
        }
        mailDispatchPersistenceService.startProcessing(job.getId());

        plan.targets().forEach(target -> sendTarget(job.getId(), target));
        return mailDispatchPersistenceService.getResult(job.getId());
    }

    private void sendTarget(Long dispatchJobId, MailDispatchPlan.Target target) {
        try {
            emailSendService.sendEmail(target.email(), target.subject(), target.body());
        } catch (Exception exception) {
            // 대상별 실패를 기록하고 다음 대상 발송을 계속한다.
            String failureReason = exception instanceof EmailException emailException
                    ? emailException.getErrorCode().getCode()
                    : MailErrorCode.MAIL_SEND_FAILURE.getCode();
            mailDispatchPersistenceService.recordFailure(
                    dispatchJobId, target.applyId(), failureReason);
            return;
        }
        mailDispatchPersistenceService.recordSuccess(dispatchJobId, target.applyId());
    }

    private void validateIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null
                || idempotencyKey.isBlank()
                || idempotencyKey.length() > MAX_IDEMPOTENCY_KEY_LENGTH) {
            throw new MailException(MailErrorCode.INVALID_IDEMPOTENCY_KEY);
        }
    }

    private void validateRequestFingerprint(Long requestedByAdminId,
                                            String idempotencyKey,
                                            String requestFingerprint) {
        Optional<String> existingFingerprint =
                mailDispatchPersistenceService.findRequestFingerprintByIdempotencyKey(
                        requestedByAdminId, idempotencyKey);
        if (existingFingerprint.isPresent() && !existingFingerprint.get().equals(requestFingerprint)) {
            throw new MailException(MailErrorCode.IDEMPOTENCY_KEY_PAYLOAD_MISMATCH);
        }
    }
}
