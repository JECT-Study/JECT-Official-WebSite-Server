package org.ject.support.admin.mail.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import org.ject.support.admin.mail.domain.MailDispatchJob;
import org.ject.support.admin.mail.domain.MailDispatchOutbox;
import org.ject.support.admin.mail.exception.MailErrorCode;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.external.email.exception.EmailErrorCode;
import org.ject.support.external.email.exception.EmailException;
import org.ject.support.external.email.service.EmailSendService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

class MailDispatchDeliveryServiceTest extends UnitTestSupport {

    @Mock
    private EmailSendService emailSendService;

    @Mock
    private MailDispatchPersistenceService mailDispatchPersistenceService;

    @Mock
    private MailDispatchRetryPolicy mailDispatchRetryPolicy;

    @InjectMocks
    private MailDispatchDeliveryService mailDispatchDeliveryService;

    @Test
    @DisplayName("Outbox 발송 성공 시 대상 성공을 기록한다")
    void Outbox_발송_성공_시_대상_성공을_기록한다() {
        // given
        MailDispatchOutbox outbox = outbox();

        // when
        mailDispatchDeliveryService.deliver(outbox);

        // then
        verify(emailSendService).sendEmail("applicant@ject.kr", "제목", "본문");
        verify(mailDispatchPersistenceService).recordSuccess(100L, 10L);
    }

    @Test
    @DisplayName("메일 공급자 오류 시 안전한 실패 코드를 기록한다")
    void 메일_공급자_오류_시_안전한_실패_코드를_기록한다() {
        // given
        MailDispatchOutbox outbox = outbox();
        willThrow(new EmailException(EmailErrorCode.EMAIL_SEND_FAILURE))
                .given(emailSendService)
                .sendEmail(any(), any(), any());

        // when
        mailDispatchDeliveryService.deliver(outbox);

        // then
        verify(mailDispatchPersistenceService).recordFailure(
                100L, 10L, EmailErrorCode.EMAIL_SEND_FAILURE.getCode());
    }

    @Test
    @DisplayName("일시적인 메일 공급자 오류는 재시도를 예약한다")
    void 일시적인_메일_공급자_오류는_재시도를_예약한다() {
        // given
        MailDispatchOutbox outbox = outbox();
        ReflectionTestUtils.setField(outbox, "id", 200L);
        ReflectionTestUtils.setField(outbox, "attemptCount", 1);
        LocalDateTime nextAttemptAt = LocalDateTime.now().plusSeconds(30);
        willThrow(new EmailException(EmailErrorCode.EMAIL_TRANSIENT_FAILURE))
                .given(emailSendService)
                .sendEmail(any(), any(), any());
        given(mailDispatchRetryPolicy.shouldRetry(EmailErrorCode.EMAIL_TRANSIENT_FAILURE.getCode(), 1))
                .willReturn(true);
        given(mailDispatchRetryPolicy.nextAttemptAt(eq(1), any(LocalDateTime.class)))
                .willReturn(nextAttemptAt);

        // when
        mailDispatchDeliveryService.deliver(outbox);

        // then
        verify(mailDispatchPersistenceService).scheduleRetry(
                200L, EmailErrorCode.EMAIL_TRANSIENT_FAILURE.getCode(), nextAttemptAt);
        verify(mailDispatchPersistenceService, never()).recordFailure(any(), any(), any());
    }

    @Test
    @DisplayName("알 수 없는 발송 오류는 공통 실패 코드로 기록한다")
    void 알_수_없는_발송_오류는_공통_실패_코드로_기록한다() {
        // given
        MailDispatchOutbox outbox = outbox();
        willThrow(new IllegalStateException("send failure"))
                .given(emailSendService)
                .sendEmail(any(), any(), any());

        // when & then
        assertThatCode(() -> mailDispatchDeliveryService.deliver(outbox))
                .doesNotThrowAnyException();
        verify(mailDispatchPersistenceService).recordFailure(
                100L, 10L, MailErrorCode.MAIL_SEND_FAILURE.getCode());
    }

    private MailDispatchOutbox outbox() {
        MailDispatchJob job = MailDispatchJob.create(
                1L, 2L, 3L, "dispatch-key", "제목", "본문", "{}", 1);
        ReflectionTestUtils.setField(job, "id", 100L);
        return MailDispatchOutbox.pending(job, 10L, "applicant@ject.kr", "제목", "본문");
    }
}
