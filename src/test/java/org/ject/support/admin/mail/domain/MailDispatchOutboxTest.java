package org.ject.support.admin.mail.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MailDispatchOutboxTest {

    @Test
    @DisplayName("발송 Outbox를 생성하면 대기 상태와 렌더링된 메일 snapshot이 저장된다")
    void 발송_Outbox를_생성하면_대기_상태와_렌더링된_메일_snapshot이_저장된다() {
        // given
        MailDispatchJob job = createJob();

        // when
        MailDispatchOutbox outbox = MailDispatchOutbox.pending(
                job,
                10L,
                "applicant@ject.kr",
                "렌더링된 제목",
                "렌더링된 본문");

        // then
        assertThat(outbox.getDispatchJob()).isSameAs(job);
        assertThat(outbox.getApplyId()).isEqualTo(10L);
        assertThat(outbox.getEmail()).isEqualTo("applicant@ject.kr");
        assertThat(outbox.getSubject()).isEqualTo("렌더링된 제목");
        assertThat(outbox.getBody()).isEqualTo("렌더링된 본문");
        assertThat(outbox.getStatus()).isEqualTo(MailDispatchOutboxStatus.PENDING);
        assertThat(outbox.getFailureReason()).isNull();
    }

    @Test
    @DisplayName("발송 Outbox를 성공 상태로 변경한다")
    void 발송_Outbox를_성공_상태로_변경한다() {
        // given
        MailDispatchOutbox outbox = createOutbox();

        // when
        outbox.markSent();

        // then
        assertThat(outbox.getStatus()).isEqualTo(MailDispatchOutboxStatus.SENT);
        assertThat(outbox.getFailureReason()).isNull();
    }

    @Test
    @DisplayName("발송 Outbox를 실패 상태와 실패 사유로 변경한다")
    void 발송_Outbox를_실패_상태와_실패_사유로_변경한다() {
        // given
        MailDispatchOutbox outbox = createOutbox();

        // when
        outbox.markFailed("이메일 전송에 실패했습니다.");

        // then
        assertThat(outbox.getStatus()).isEqualTo(MailDispatchOutboxStatus.FAILED);
        assertThat(outbox.getFailureReason()).isEqualTo("이메일 전송에 실패했습니다.");
    }

    private MailDispatchOutbox createOutbox() {
        return MailDispatchOutbox.pending(
                createJob(),
                10L,
                "applicant@ject.kr",
                "제목",
                "본문");
    }

    private MailDispatchJob createJob() {
        return MailDispatchJob.create(1L, 2L, 3L, "dispatch-key", "제목", "본문", "{}", 1);
    }
}
