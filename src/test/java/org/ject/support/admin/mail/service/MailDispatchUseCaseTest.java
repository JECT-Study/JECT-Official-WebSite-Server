package org.ject.support.admin.mail.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.ject.support.admin.mail.domain.MailDispatchJob;
import org.ject.support.admin.mail.domain.MailDispatchJobStatus;
import org.ject.support.admin.mail.dto.MailDispatchResponse;
import org.ject.support.admin.mail.dto.SendMailDispatchRequest;
import org.ject.support.admin.mail.exception.MailErrorCode;
import org.ject.support.admin.mail.exception.MailException;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.external.email.exception.EmailErrorCode;
import org.ject.support.external.email.exception.EmailException;
import org.ject.support.external.email.service.EmailSendService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

class MailDispatchUseCaseTest extends UnitTestSupport {

    @Mock
    private MailDispatchPreparationService preparationService;

    @Mock
    private MailDispatchPersistenceService persistenceService;

    @Mock
    private EmailSendService emailSendService;

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
        given(persistenceService.findResultByIdempotencyKey(3L, "dispatch-key"))
                .willReturn(Optional.empty());
        given(preparationService.prepare(request, 3L, "dispatch-key")).willReturn(plan);
        given(persistenceService.createJob(plan)).willReturn(job);
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
        given(persistenceService.findResultByIdempotencyKey(3L, "dispatch-key"))
                .willReturn(Optional.empty());
        given(preparationService.prepare(request, 3L, "dispatch-key")).willReturn(plan);
        given(persistenceService.createJob(plan)).willReturn(job);
        given(persistenceService.getResult(100L)).willReturn(response);
        doThrow(new EmailException(EmailErrorCode.EMAIL_SEND_FAILURE))
                .when(emailSendService).sendEmail("one@ject.kr", "첫 번째", "본문 1");

        // when
        MailDispatchResponse result = mailDispatchUseCase.sendMail(request, 3L, "dispatch-key");

        // then
        assertThat(result.failedCount()).isEqualTo(1);
        verify(persistenceService).recordFailure(
                100L, 1L, MailErrorCode.MAIL_SEND_FAILURE.getMessage());
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
        given(persistenceService.findResultByIdempotencyKey(3L, "dispatch-key"))
                .willReturn(Optional.empty());
        given(preparationService.prepare(request, 3L, "dispatch-key"))
                .willThrow(new MailException(MailErrorCode.INVALID_DISPATCH_TARGETS));

        // when & then
        assertThatThrownBy(() -> mailDispatchUseCase.sendMail(request, 3L, "dispatch-key"))
                .isInstanceOf(MailException.class);
        verify(persistenceService, never()).createJob(any());
        verifyNoInteractions(emailSendService);
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

        // when
        MailDispatchResponse result = mailDispatchUseCase.sendMail(request, 3L, "dispatch-key");

        // then
        assertThat(result).isEqualTo(response);
        verifyNoInteractions(preparationService, emailSendService);
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
}
