package org.ject.support.admin.mail.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.hibernate.exception.ConstraintViolationException;
import org.ject.support.admin.mail.domain.MailDispatchJob;
import org.ject.support.admin.mail.domain.MailDispatchJobStatus;
import org.ject.support.admin.mail.dto.MailDispatchResponse;
import org.ject.support.admin.mail.dto.SendMailDispatchRequest;
import org.ject.support.admin.mail.exception.MailErrorCode;
import org.ject.support.admin.mail.exception.MailException;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.common.util.Map2JsonSerializer;
import org.ject.support.external.email.exception.EmailErrorCode;
import org.ject.support.external.email.exception.EmailException;
import org.ject.support.external.email.service.EmailSendService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

class MailDispatchUseCaseTest extends UnitTestSupport {

    @Mock
    private MailDispatchPreparationService preparationService;

    @Mock
    private MailDispatchPersistenceService persistenceService;

    @Mock
    private EmailSendService emailSendService;

    @Spy
    private MailDispatchRequestFingerprintGenerator requestFingerprintGenerator =
            new MailDispatchRequestFingerprintGenerator(new Map2JsonSerializer(new ObjectMapper()));

    @InjectMocks
    private MailDispatchUseCase mailDispatchUseCase;

    @Test
    @DisplayName("대상별 발송에 성공하면 완료 결과를 반환한다")
    void 대상별_발송에_성공하면_완료_결과를_반환한다() {
        // given
        SendMailDispatchRequest request = request();
        MailDispatchPlan plan = plan();
        MailDispatchJob job = job(100L);
        MailDispatchResponse response = new MailDispatchResponse(
                100L, MailDispatchJobStatus.COMPLETED, 2, 0, 2, 0);
        given(persistenceService.findJobByIdempotencyKey(3L, "dispatch-key"))
                .willReturn(Optional.empty());
        given(preparationService.prepare(request, 3L, "dispatch-key")).willReturn(plan);
        given(persistenceService.createJob(plan, requestFingerprintGenerator.generate(request))).willReturn(job);
        given(persistenceService.getResult(100L)).willReturn(response);

        // when
        MailDispatchResponse result = mailDispatchUseCase.sendMail(request, 3L, "dispatch-key");

        // then
        assertThat(result).isEqualTo(response);
        verify(emailSendService).sendEmail("one@ject.kr", "첫 번째", "본문 1");
        verify(emailSendService).sendEmail("two@ject.kr", "두 번째", "본문 2");
        verify(persistenceService).recordSuccess(100L, 1L);
        verify(persistenceService).recordSuccess(100L, 2L);
    }

    @Test
    @DisplayName("한 대상 발송에 실패해도 나머지 대상을 계속 발송하고 실패를 기록한다")
    void 한_대상_발송에_실패해도_나머지_대상을_계속_발송하고_실패를_기록한다() {
        // given
        SendMailDispatchRequest request = request();
        MailDispatchPlan plan = plan();
        MailDispatchJob job = job(100L);
        MailDispatchResponse response = new MailDispatchResponse(
                100L, MailDispatchJobStatus.COMPLETED, 2, 0, 1, 1);
        given(persistenceService.findJobByIdempotencyKey(3L, "dispatch-key"))
                .willReturn(Optional.empty());
        given(preparationService.prepare(request, 3L, "dispatch-key")).willReturn(plan);
        given(persistenceService.createJob(plan, requestFingerprintGenerator.generate(request))).willReturn(job);
        given(persistenceService.getResult(100L)).willReturn(response);
        doThrow(new EmailException(EmailErrorCode.EMAIL_SEND_FAILURE))
                .when(emailSendService).sendEmail("one@ject.kr", "첫 번째", "본문 1");

        // when
        MailDispatchResponse result = mailDispatchUseCase.sendMail(request, 3L, "dispatch-key");

        // then
        assertThat(result.failedCount()).isEqualTo(1);
        verify(persistenceService).recordFailure(
                100L, 1L, EmailErrorCode.EMAIL_SEND_FAILURE.getCode());
        verify(persistenceService).recordSuccess(100L, 2L);
        InOrder order = inOrder(emailSendService);
        order.verify(emailSendService).sendEmail("one@ject.kr", "첫 번째", "본문 1");
        order.verify(emailSendService).sendEmail("two@ject.kr", "두 번째", "본문 2");
    }

    @Test
    @DisplayName("대상 검증에 실패하면 작업을 저장하거나 메일을 발송하지 않는다")
    void 대상_검증에_실패하면_작업을_저장하거나_메일을_발송하지_않는다() {
        // given
        SendMailDispatchRequest request = request();
        given(persistenceService.findJobByIdempotencyKey(3L, "dispatch-key"))
                .willReturn(Optional.empty());
        given(preparationService.prepare(request, 3L, "dispatch-key"))
                .willThrow(new MailException(MailErrorCode.INVALID_DISPATCH_TARGETS));

        // when & then
        assertThatThrownBy(() -> mailDispatchUseCase.sendMail(request, 3L, "dispatch-key"))
                .isInstanceOf(MailException.class);
        verify(persistenceService, never()).createJob(any(), any());
        verifyNoInteractions(emailSendService);
    }

    @Test
    @DisplayName("같은 Idempotency-Key로 재요청하면 기존 결과를 반환하고 다시 발송하지 않는다")
    void 같은_Idempotency_Key로_재요청하면_기존_결과를_반환하고_다시_발송하지_않는다() {
        // given
        SendMailDispatchRequest request = request();
        String fingerprint = requestFingerprintGenerator.generate(request);
        MailDispatchJob existingJob = completedJob(100L, fingerprint, 2);
        MailDispatchResponse response = MailDispatchResponse.from(existingJob);
        given(persistenceService.findJobByIdempotencyKey(3L, "dispatch-key"))
                .willReturn(Optional.of(existingJob));

        // when
        MailDispatchResponse result = mailDispatchUseCase.sendMail(request, 3L, "dispatch-key");

        // then
        assertThat(result).isEqualTo(response);
        verify(persistenceService, times(1)).findJobByIdempotencyKey(3L, "dispatch-key");
        verifyNoInteractions(preparationService, emailSendService);
    }

    @Test
    @DisplayName("같은 관리자와 키에 다른 요청 내용을 보내면 충돌한다")
    void 같은_관리자와_키에_다른_요청_내용을_보내면_충돌한다() {
        // given
        SendMailDispatchRequest differentRequest = new SendMailDispatchRequest(
                2L, 1L, List.of(1L, 2L), "다른 제목", Map.of());
        MailDispatchJob existingJob = completedJob(100L, requestFingerprintGenerator.generate(request()), 2);
        given(persistenceService.findJobByIdempotencyKey(3L, "dispatch-key"))
                .willReturn(Optional.of(existingJob));

        // when & then
        assertThatThrownBy(() -> mailDispatchUseCase.sendMail(differentRequest, 3L, "dispatch-key"))
                .isInstanceOf(MailException.class)
                .extracting("errorCode")
                .isEqualTo(MailErrorCode.IDEMPOTENCY_KEY_PAYLOAD_MISMATCH);
    }

    @Test
    @DisplayName("순서만 다른 재요청은 기존 작업을 반환한다")
    void 순서만_다른_재요청은_기존_작업을_반환한다() {
        // given
        SendMailDispatchRequest originalRequest = new SendMailDispatchRequest(
                2L, 1L, List.of(1L, 2L), "제목", Map.of("A", "첫 번째", "B", "두 번째"));
        Map<String, String> reorderedVariables = new LinkedHashMap<>();
        reorderedVariables.put("B", "두 번째");
        reorderedVariables.put("A", "첫 번째");
        SendMailDispatchRequest reorderedRequest = new SendMailDispatchRequest(
                2L, 1L, List.of(2L, 1L), "제목", reorderedVariables);
        String originalFingerprint = requestFingerprintGenerator.generate(originalRequest);
        MailDispatchJob existingJob = completedJob(100L, originalFingerprint, 2);
        MailDispatchResponse response = MailDispatchResponse.from(existingJob);
        given(persistenceService.findJobByIdempotencyKey(3L, "dispatch-key"))
                .willReturn(Optional.of(existingJob));

        // when
        MailDispatchResponse result = mailDispatchUseCase.sendMail(reorderedRequest, 3L, "dispatch-key");

        // then
        assertThat(result).isEqualTo(response);
        verifyNoInteractions(preparationService, emailSendService);
    }

    @Test
    @DisplayName("다른 관리자는 같은 키로 별도 발송을 시작한다")
    void 다른_관리자는_같은_키로_별도_발송을_시작한다() {
        // given
        SendMailDispatchRequest request = request();
        MailDispatchPlan otherAdminPlan = new MailDispatchPlan(
                1L,
                2L,
                4L,
                "dispatch-key",
                "제목 템플릿",
                "본문 템플릿",
                Map.of(),
                List.of(new MailDispatchPlan.Target(1L, "one@ject.kr", "제목", "본문")));
        MailDispatchJob otherAdminJob = MailDispatchJob.create(
                1L, 2L, 4L, "dispatch-key", "제목", "본문", "{}", 1);
        ReflectionTestUtils.setField(otherAdminJob, "id", 101L);
        MailDispatchResponse response = new MailDispatchResponse(
                101L, MailDispatchJobStatus.COMPLETED, 1, 0, 1, 0);
        String fingerprint = requestFingerprintGenerator.generate(request);
        given(persistenceService.findJobByIdempotencyKey(4L, "dispatch-key"))
                .willReturn(Optional.empty());
        given(preparationService.prepare(request, 4L, "dispatch-key")).willReturn(otherAdminPlan);
        given(persistenceService.createJob(otherAdminPlan, fingerprint)).willReturn(otherAdminJob);
        given(persistenceService.getResult(101L)).willReturn(response);

        // when
        MailDispatchResponse result = mailDispatchUseCase.sendMail(request, 4L, "dispatch-key");

        // then
        assertThat(result).isEqualTo(response);
        verify(persistenceService).findJobByIdempotencyKey(4L, "dispatch-key");
        verify(emailSendService).sendEmail("one@ject.kr", "제목", "본문");
    }

    @Test
    @DisplayName("fingerprint가 없는 기존 작업은 요청 내용을 비교할 수 없어 충돌한다")
    void fingerprint가_없는_기존_작업은_요청을_충돌로_거부한다() {
        // given
        SendMailDispatchRequest differentRequest = new SendMailDispatchRequest(
                2L, 1L, List.of(1L, 2L), "다른 제목", Map.of());
        MailDispatchJob existingJob = completedJob(100L, null, 2);
        given(persistenceService.findJobByIdempotencyKey(3L, "dispatch-key"))
                .willReturn(Optional.of(existingJob));

        // when & then
        assertThatThrownBy(() -> mailDispatchUseCase.sendMail(differentRequest, 3L, "dispatch-key"))
                .isInstanceOf(MailException.class)
                .extracting("errorCode")
                .isEqualTo(MailErrorCode.IDEMPOTENCY_KEY_PAYLOAD_MISMATCH);
        verify(persistenceService, never()).createJob(any(), any());
        verifyNoInteractions(preparationService, emailSendService);
    }

    @Test
    @DisplayName("동시 요청으로 같은 키가 저장된 뒤 같은 요청이면 기존 결과를 반환한다")
    void 동시_요청으로_같은_키가_저장된_뒤_같은_요청이면_기존_결과를_반환한다() {
        // given
        SendMailDispatchRequest request = request();
        MailDispatchPlan plan = plan();
        String fingerprint = requestFingerprintGenerator.generate(request);
        MailDispatchJob concurrentJob = completedJob(100L, fingerprint, 2);
        MailDispatchResponse response = MailDispatchResponse.from(concurrentJob);
        given(persistenceService.findJobByIdempotencyKey(3L, "dispatch-key"))
                .willReturn(Optional.empty(), Optional.of(concurrentJob));
        given(preparationService.prepare(request, 3L, "dispatch-key")).willReturn(plan);
        given(persistenceService.createJob(plan, fingerprint))
                .willThrow(idempotencyKeyConflict());

        // when
        MailDispatchResponse result = mailDispatchUseCase.sendMail(request, 3L, "dispatch-key");

        // then
        assertThat(result).isEqualTo(response);
        verifyNoInteractions(emailSendService);
        verify(persistenceService, times(2)).findJobByIdempotencyKey(3L, "dispatch-key");
    }

    @Test
    @DisplayName("idempotency 유니크 충돌이 아닌 무결성 오류는 다시 던진다")
    void idempotency_유니크_충돌이_아닌_무결성_오류는_다시_던진다() {
        // given
        SendMailDispatchRequest request = request();
        MailDispatchPlan plan = plan();
        String fingerprint = requestFingerprintGenerator.generate(request);
        MailDispatchJob concurrentJob = completedJob(100L, fingerprint, 2);
        SQLException sqlException = new SQLException("duplicate target", "23000", 1062);
        ConstraintViolationException constraintViolation = new ConstraintViolationException(
                "target unique constraint violated", sqlException, "uk_mail_dispatch_target_job_apply");
        DataIntegrityViolationException integrityException = new DataIntegrityViolationException(
                "target unique constraint violated", constraintViolation);
        given(persistenceService.findJobByIdempotencyKey(3L, "dispatch-key"))
                .willReturn(Optional.empty(), Optional.of(concurrentJob));
        given(preparationService.prepare(request, 3L, "dispatch-key")).willReturn(plan);
        given(persistenceService.createJob(plan, fingerprint)).willThrow(integrityException);

        // when & then
        assertThatThrownBy(() -> mailDispatchUseCase.sendMail(request, 3L, "dispatch-key"))
                .isSameAs(integrityException);
        verify(persistenceService).findJobByIdempotencyKey(3L, "dispatch-key");
        verifyNoInteractions(emailSendService);
    }

    @Test
    @DisplayName("동시 요청으로 같은 키가 저장된 뒤 다른 요청이면 충돌한다")
    void 동시_요청으로_같은_키가_저장된_뒤_다른_요청이면_충돌한다() {
        // given
        SendMailDispatchRequest request = request();
        MailDispatchPlan plan = plan();
        String fingerprint = requestFingerprintGenerator.generate(request);
        MailDispatchJob concurrentJob = completedJob(100L, "different-fingerprint", 2);
        MailDispatchResponse response = MailDispatchResponse.from(concurrentJob);
        given(persistenceService.findJobByIdempotencyKey(3L, "dispatch-key"))
                .willReturn(Optional.empty(), Optional.of(concurrentJob));
        given(preparationService.prepare(request, 3L, "dispatch-key")).willReturn(plan);
        given(persistenceService.createJob(plan, fingerprint))
                .willThrow(idempotencyKeyConflict());

        // when & then
        assertThatThrownBy(() -> mailDispatchUseCase.sendMail(request, 3L, "dispatch-key"))
                .isInstanceOf(MailException.class)
                .extracting("errorCode")
                .isEqualTo(MailErrorCode.IDEMPOTENCY_KEY_PAYLOAD_MISMATCH);
        verifyNoInteractions(emailSendService);
        verify(persistenceService, times(2)).findJobByIdempotencyKey(3L, "dispatch-key");
    }

    @Test
    @DisplayName("Idempotency-Key가 비어 있으면 발송하지 않는다")
    void Idempotency_Key가_비어_있으면_발송하지_않는다() {
        // when & then
        assertThatThrownBy(() -> mailDispatchUseCase.sendMail(request(), 3L, " "))
                .isInstanceOf(MailException.class)
                .extracting("errorCode")
                .isEqualTo(MailErrorCode.INVALID_IDEMPOTENCY_KEY);
        verifyNoInteractions(preparationService, persistenceService, emailSendService);
    }

    private SendMailDispatchRequest request() {
        return new SendMailDispatchRequest(2L, 1L, List.of(1L, 2L), null, Map.of());
    }

    private MailDispatchPlan plan() {
        return new MailDispatchPlan(
                1L,
                2L,
                3L,
                "dispatch-key",
                "제목 템플릿",
                "본문 템플릿",
                Map.of(),
                List.of(
                        new MailDispatchPlan.Target(1L, "one@ject.kr", "첫 번째", "본문 1"),
                        new MailDispatchPlan.Target(2L, "two@ject.kr", "두 번째", "본문 2")
                ));
    }

    private MailDispatchJob job(Long id) {
        MailDispatchJob job = MailDispatchJob.create(
                1L, 2L, 3L, "dispatch-key", "제목", "본문", "{}", 2);
        ReflectionTestUtils.setField(job, "id", id);
        return job;
    }

    private MailDispatchJob completedJob(Long id, String fingerprint, int targetCount) {
        MailDispatchJob job = MailDispatchJob.create(
                1L, 2L, 3L, "dispatch-key", "제목", "본문", "{}", fingerprint, targetCount);
        ReflectionTestUtils.setField(job, "id", id);
        job.startProcessing();
        for (int index = 0; index < targetCount; index++) {
            job.recordSuccess();
        }
        return job;
    }

    private DataIntegrityViolationException idempotencyKeyConflict() {
        SQLException sqlException = new SQLException("duplicate key", "23000", 1062);
        ConstraintViolationException constraintViolation = new ConstraintViolationException(
                "unique constraint violated", sqlException, "uk_mail_dispatch_job_requester_key");
        return new DataIntegrityViolationException("idempotency key already exists", constraintViolation);
    }
}
