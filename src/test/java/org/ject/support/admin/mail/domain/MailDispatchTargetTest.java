package org.ject.support.admin.mail.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.ject.support.admin.mail.exception.MailException;

class MailDispatchTargetTest {

    @Test
    @DisplayName("발송 대상을 생성하면 대기 상태와 이메일 snapshot이 저장된다")
    void 발송_대상을_생성하면_대기_상태와_이메일_snapshot이_저장된다() {
        // given
        MailDispatchJob job = MailDispatchJob.create(1L, 2L, 3L, "제목", "본문", "{}", 1);

        // when
        MailDispatchTarget target = MailDispatchTarget.pending(job, 10L, "applicant@ject.kr");

        // then
        assertThat(target.getDispatchJob()).isSameAs(job);
        assertThat(target.getApplyId()).isEqualTo(10L);
        assertThat(target.getEmail()).isEqualTo("applicant@ject.kr");
        assertThat(target.getStatus()).isEqualTo(MailDispatchTargetStatus.PENDING);
    }

    @Test
    @DisplayName("발송 대상을 성공 상태로 변경한다")
    void 발송_대상을_성공_상태로_변경한다() {
        // given
        MailDispatchTarget target = MailDispatchTarget.pending(
                MailDispatchJob.create(1L, 2L, 3L, "제목", "본문", "{}", 1),
                10L,
                "applicant@ject.kr");

        // when
        target.markSent();

        // then
        assertThat(target.getStatus()).isEqualTo(MailDispatchTargetStatus.SENT);
        assertThat(target.getSentAt()).isNotNull();
        assertThat(target.getFailureReason()).isNull();
    }

    @Test
    @DisplayName("발송 대상을 실패 상태와 실패 사유로 변경한다")
    void 발송_대상을_실패_상태와_실패_사유로_변경한다() {
        // given
        MailDispatchTarget target = MailDispatchTarget.pending(
                MailDispatchJob.create(1L, 2L, 3L, "제목", "본문", "{}", 1),
                10L,
                "applicant@ject.kr");

        // when
        target.markFailed("이메일 전송에 실패했습니다.");

        // then
        assertThat(target.getStatus()).isEqualTo(MailDispatchTargetStatus.FAILED);
        assertThat(target.getFailureReason()).isEqualTo("이메일 전송에 실패했습니다.");
        assertThat(target.getSentAt()).isNull();
    }

    @Test
    @DisplayName("처리된 발송 대상은 다시 처리할 수 없다")
    void 처리된_발송_대상은_다시_처리할_수_없다() {
        // given
        MailDispatchTarget target = MailDispatchTarget.pending(
                MailDispatchJob.create(1L, 2L, 3L, "제목", "본문", "{}", 1),
                10L,
                "applicant@ject.kr");
        target.markSent();

        // when & then
        assertThatThrownBy(target::markSent)
                .isInstanceOf(MailException.class);
    }
}
