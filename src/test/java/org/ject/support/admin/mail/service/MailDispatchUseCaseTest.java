package org.ject.support.admin.mail.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.ject.support.admin.mail.domain.MailDispatchJob;
import org.ject.support.admin.mail.domain.MailDispatchJobStatus;
import org.ject.support.admin.mail.domain.MailDispatchOutbox;
import org.ject.support.admin.mail.dto.MailDispatchResponse;
import org.ject.support.admin.mail.dto.SendMailDispatchRequest;
import org.ject.support.admin.mail.exception.MailErrorCode;
import org.ject.support.admin.mail.exception.MailException;
import org.ject.support.base.UnitTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

class MailDispatchUseCaseTest extends UnitTestSupport {

    @Mock
    private MailDispatchPreparationService preparationService;

    @Mock
    private MailDispatchPersistenceService persistenceService;

    @Mock
    private MailDispatchDeliveryService mailDispatchDeliveryService;

    @Mock
    private MailDispatchRequestFingerprint requestFingerprint;

    @InjectMocks
    private MailDispatchUseCase mailDispatchUseCase;

    @BeforeEach
    void setUp() {
        Mockito.lenient().when(requestFingerprint.generate(any())).thenReturn("fingerprint");
    }

    @Test
    @DisplayName("대상별 발송에 성공하면 완료 결과를 반환한다")
    void 대상별_발송에_성공하면_완료_결과를_반환한다() {
        // given
        SendMailDispatchRequest request = request();
        MailDispatchPlan plan = plan();
        MailDispatchJob job = job(100L);
        MailDispatchResponse response = new MailDispatchResponse(
                100L, MailDispatchJobStatus.COMPLETED, 2, 0, 2, 0);
        given(persistenceService.findResultByIdempotencyKey(3L, "dispatch-key"))
                .willReturn(Optional.empty());
        given(preparationService.prepare(request, 3L, "dispatch-key")).willReturn(plan);
        given(persistenceService.createJob(plan, "fingerprint")).willReturn(job);
        given(persistenceService.claimForImmediateDispatch(100L, 1L, "request:100"))
                .willReturn(Optional.of(outbox(job, 1L, "one@ject.kr", "첫 번째", "본문 1")));
        given(persistenceService.claimForImmediateDispatch(100L, 2L, "request:100"))
                .willReturn(Optional.of(outbox(job, 2L, "two@ject.kr", "두 번째", "본문 2")));
        given(persistenceService.getResult(100L)).willReturn(response);

        // when
        MailDispatchResponse result = mailDispatchUseCase.sendMail(request, 3L, "dispatch-key");

        // then
        assertThat(result).isEqualTo(response);
        verify(mailDispatchDeliveryService, org.mockito.Mockito.times(2))
                .deliver(any(MailDispatchOutbox.class));
    }

    @Test
    @DisplayName("발송 대상별 Outbox를 delivery service에 위임한다")
    void 발송_대상별_Outbox를_delivery_service에_위임한다() {
        // given
        SendMailDispatchRequest request = request();
        MailDispatchPlan plan = plan();
        MailDispatchJob job = job(100L);
        MailDispatchResponse response = new MailDispatchResponse(
                100L, MailDispatchJobStatus.COMPLETED, 2, 0, 1, 1);
        given(persistenceService.findResultByIdempotencyKey(3L, "dispatch-key"))
                .willReturn(Optional.empty());
        given(preparationService.prepare(request, 3L, "dispatch-key")).willReturn(plan);
        given(persistenceService.createJob(plan, "fingerprint")).willReturn(job);
        given(persistenceService.claimForImmediateDispatch(100L, 1L, "request:100"))
                .willReturn(Optional.of(outbox(job, 1L, "one@ject.kr", "첫 번째", "본문 1")));
        given(persistenceService.claimForImmediateDispatch(100L, 2L, "request:100"))
                .willReturn(Optional.of(outbox(job, 2L, "two@ject.kr", "두 번째", "본문 2")));
        given(persistenceService.getResult(100L)).willReturn(response);

        // when
        MailDispatchResponse result = mailDispatchUseCase.sendMail(request, 3L, "dispatch-key");

        // then
        assertThat(result.failedCount()).isEqualTo(1);
        verify(mailDispatchDeliveryService, org.mockito.Mockito.times(2))
                .deliver(any(MailDispatchOutbox.class));
    }

    @Test
    @DisplayName("대상 검증에 실패하면 작업을 저장하거나 메일을 발송하지 않는다")
    void 대상_검증에_실패하면_작업을_저장하거나_메일을_발송하지_않는다() {
        // given
        SendMailDispatchRequest request = request();
        given(persistenceService.findResultByIdempotencyKey(3L, "dispatch-key"))
                .willReturn(Optional.empty());
        given(preparationService.prepare(request, 3L, "dispatch-key"))
                .willThrow(new MailException(MailErrorCode.INVALID_DISPATCH_TARGETS));

        // when & then
        assertThatThrownBy(() -> mailDispatchUseCase.sendMail(request, 3L, "dispatch-key"))
                .isInstanceOf(MailException.class);
        verify(persistenceService, never()).createJob(any(), any());
        verifyNoInteractions(mailDispatchDeliveryService);
    }

    @Test
    @DisplayName("같은 Idempotency-Key로 재요청하면 기존 결과를 반환하고 다시 발송하지 않는다")
    void 같은_Idempotency_Key로_재요청하면_기존_결과를_반환하고_다시_발송하지_않는다() {
        // given
        SendMailDispatchRequest request = request();
        MailDispatchResponse response = new MailDispatchResponse(
                100L, MailDispatchJobStatus.COMPLETED, 2, 0, 2, 0);
        given(persistenceService.findResultByIdempotencyKey(3L, "dispatch-key"))
                .willReturn(Optional.of(response));
        given(persistenceService.findRequestFingerprintByIdempotencyKey(3L, "dispatch-key"))
                .willReturn(Optional.of("fingerprint"));

        // when
        MailDispatchResponse result = mailDispatchUseCase.sendMail(request, 3L, "dispatch-key");

        // then
        assertThat(result).isEqualTo(response);
        verifyNoInteractions(preparationService, mailDispatchDeliveryService);
    }

    @Test
    @DisplayName("같은 Idempotency-Key에 다른 요청 본문을 사용하면 충돌을 반환한다")
    void 같은_Idempotency_Key에_다른_요청_본문을_사용하면_충돌을_반환한다() {
        // given
        SendMailDispatchRequest request = request();
        MailDispatchResponse response = new MailDispatchResponse(
                100L, MailDispatchJobStatus.COMPLETED, 2, 0, 2, 0);
        given(requestFingerprint.generate(request)).willReturn("new-fingerprint");
        given(persistenceService.findResultByIdempotencyKey(3L, "dispatch-key"))
                .willReturn(Optional.of(response));
        given(persistenceService.findRequestFingerprintByIdempotencyKey(3L, "dispatch-key"))
                .willReturn(Optional.of("old-fingerprint"));

        // when & then
        assertThatThrownBy(() -> mailDispatchUseCase.sendMail(request, 3L, "dispatch-key"))
                .isInstanceOf(MailException.class)
                .extracting("errorCode")
                .isEqualTo(MailErrorCode.IDEMPOTENCY_KEY_PAYLOAD_MISMATCH);
        verifyNoInteractions(preparationService, mailDispatchDeliveryService);
    }

    @Test
    @DisplayName("fingerprint가 없는 기존 작업은 기존 결과를 재사용한다")
    void fingerprint가_없는_기존_작업은_기존_결과를_재사용한다() {
        // given
        SendMailDispatchRequest request = request();
        MailDispatchResponse response = new MailDispatchResponse(
                100L, MailDispatchJobStatus.COMPLETED, 2, 0, 2, 0);
        given(persistenceService.findResultByIdempotencyKey(3L, "dispatch-key"))
                .willReturn(Optional.of(response));
        given(persistenceService.findRequestFingerprintByIdempotencyKey(3L, "dispatch-key"))
                .willReturn(Optional.empty());

        // when
        MailDispatchResponse result = mailDispatchUseCase.sendMail(request, 3L, "dispatch-key");

        // then
        assertThat(result).isEqualTo(response);
        verifyNoInteractions(preparationService, mailDispatchDeliveryService);
    }

    @Test
    @DisplayName("동시 요청으로 중복 키가 저장된 뒤 다른 본문이면 충돌을 반환한다")
    void 동시_요청으로_중복_키가_저장된_뒤_다른_본문이면_충돌을_반환한다() {
        // given
        SendMailDispatchRequest request = request();
        MailDispatchPlan plan = plan();
        MailDispatchResponse response = new MailDispatchResponse(
                100L, MailDispatchJobStatus.COMPLETED, 2, 0, 2, 0);
        given(persistenceService.findResultByIdempotencyKey(3L, "dispatch-key"))
                .willReturn(Optional.empty(), Optional.of(response));
        given(preparationService.prepare(request, 3L, "dispatch-key")).willReturn(plan);
        given(persistenceService.createJob(plan, "fingerprint"))
                .willThrow(new DataIntegrityViolationException("duplicate key"));
        given(persistenceService.findRequestFingerprintByIdempotencyKey(3L, "dispatch-key"))
                .willReturn(Optional.of("old-fingerprint"));

        // when & then
        assertThatThrownBy(() -> mailDispatchUseCase.sendMail(request, 3L, "dispatch-key"))
                .isInstanceOf(MailException.class)
                .extracting("errorCode")
                .isEqualTo(MailErrorCode.IDEMPOTENCY_KEY_PAYLOAD_MISMATCH);
        verifyNoInteractions(mailDispatchDeliveryService);
    }

    @Test
    @DisplayName("동시 요청으로 중복 키가 저장된 뒤 같은 본문이면 기존 결과를 반환한다")
    void 동시_요청으로_중복_키가_저장된_뒤_같은_본문이면_기존_결과를_반환한다() {
        // given
        SendMailDispatchRequest request = request();
        MailDispatchPlan plan = plan();
        MailDispatchResponse response = new MailDispatchResponse(
                100L, MailDispatchJobStatus.COMPLETED, 2, 0, 2, 0);
        given(persistenceService.findResultByIdempotencyKey(3L, "dispatch-key"))
                .willReturn(Optional.empty(), Optional.of(response));
        given(preparationService.prepare(request, 3L, "dispatch-key")).willReturn(plan);
        given(persistenceService.createJob(plan, "fingerprint"))
                .willThrow(new DataIntegrityViolationException("duplicate key"));
        given(persistenceService.findRequestFingerprintByIdempotencyKey(3L, "dispatch-key"))
                .willReturn(Optional.of("fingerprint"));

        // when
        MailDispatchResponse result = mailDispatchUseCase.sendMail(request, 3L, "dispatch-key");

        // then
        assertThat(result).isEqualTo(response);
        verifyNoInteractions(mailDispatchDeliveryService);
    }

    @Test
    @DisplayName("Idempotency-Key가 비어 있으면 발송하지 않는다")
    void Idempotency_Key가_비어_있으면_발송하지_않는다() {
        // when & then
        assertThatThrownBy(() -> mailDispatchUseCase.sendMail(request(), 3L, " "))
                .isInstanceOf(MailException.class)
                .extracting("errorCode")
                .isEqualTo(MailErrorCode.INVALID_IDEMPOTENCY_KEY);
        verifyNoInteractions(preparationService, persistenceService, mailDispatchDeliveryService);
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

    private MailDispatchOutbox outbox(MailDispatchJob job,
                                      Long applyId,
                                      String email,
                                      String subject,
                                      String body) {
        return MailDispatchOutbox.pending(job, applyId, email, subject, body);
    }
}
