package org.ject.support.admin.mail.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
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
    private static final String IDEMPOTENCY_KEY_CONSTRAINT_NAME = "uk_mail_dispatch_job_requester_key";
    private static final String MYSQL_IDEMPOTENCY_KEY_CONSTRAINT_NAME =
            "mail_dispatch_job." + IDEMPOTENCY_KEY_CONSTRAINT_NAME;

    private final MailDispatchPreparationService mailDispatchPreparationService;
    private final MailDispatchPersistenceService mailDispatchPersistenceService;
    private final EmailSendService emailSendService;
    private final MailDispatchRequestFingerprintGenerator requestFingerprintGenerator;

    public MailDispatchResponse sendMail(SendMailDispatchRequest request,
                                         Long requestedByAdminId,
                                         String idempotencyKey) {
        validateIdempotencyKey(idempotencyKey);
        String requestFingerprintValue = requestFingerprintGenerator.generate(request);
        Optional<MailDispatchJob> existingJob =
                mailDispatchPersistenceService.findJobByIdempotencyKey(
                        requestedByAdminId, idempotencyKey);
        if (existingJob.isPresent()) {
            return reuseExistingJob(existingJob.get(), requestFingerprintValue);
        }

        MailDispatchPlan plan = mailDispatchPreparationService.prepare(
                request, requestedByAdminId, idempotencyKey);
        MailDispatchJob job;
        try {
            job = mailDispatchPersistenceService.createJob(plan, requestFingerprintValue);
        } catch (DataIntegrityViolationException exception) {
            if (!isIdempotencyKeyConflict(exception)) {
                throw exception;
            }
            // 요청 키 유니크 충돌만 재조회해 기존 요청 fingerprint를 검증한다.
            Optional<MailDispatchJob> concurrentJob =
                    mailDispatchPersistenceService.findJobByIdempotencyKey(
                            requestedByAdminId, idempotencyKey);
            if (concurrentJob.isEmpty()) {
                throw exception;
            }
            return reuseExistingJob(concurrentJob.get(), requestFingerprintValue);
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

    private MailDispatchResponse reuseExistingJob(MailDispatchJob existingJob, String requestFingerprint) {
        // V45 이전 작업은 본문을 비교할 수 없어 같은 키 재사용을 거부한다.
        String existingFingerprint = existingJob.getRequestFingerprint();
        if (existingFingerprint == null || !existingFingerprint.equals(requestFingerprint)) {
            throw new MailException(MailErrorCode.IDEMPOTENCY_KEY_PAYLOAD_MISMATCH);
        }
        return MailDispatchResponse.from(existingJob);
    }

    private boolean isIdempotencyKeyConflict(DataIntegrityViolationException exception) {
        Throwable cause = exception;
        while (cause != null) {
            if (cause instanceof ConstraintViolationException constraintViolation) {
                String constraintName = constraintViolation.getConstraintName();
                if (IDEMPOTENCY_KEY_CONSTRAINT_NAME.equals(constraintName)
                        || MYSQL_IDEMPOTENCY_KEY_CONSTRAINT_NAME.equals(constraintName)) {
                    return true;
                }
            }
            cause = cause.getCause();
        }
        return false;
    }
}
