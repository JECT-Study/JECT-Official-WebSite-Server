package org.ject.support.admin.mail.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.ject.support.admin.mail.exception.MailErrorCode;
import org.ject.support.admin.mail.exception.MailException;

class MailDispatchJobTest {

    @Test
    @DisplayName("발송 작업을 생성하면 요청 상태와 대상 건수가 설정된다")
    void 발송_작업을_생성하면_요청_상태와_대상_건수가_설정된다() {
        // given
        MailDispatchJob job = MailDispatchJob.create(
                1L,
                2L,
                3L,
                "dispatch-key",
                "제목 템플릿",
                "본문 템플릿",
                "{}",
                2);

        // when & then
        assertThat(job.getScenarioId()).isEqualTo(1L);
        assertThat(job.getRecruitId()).isEqualTo(2L);
        assertThat(job.getRequestedByAdminId()).isEqualTo(3L);
        assertThat(job.getIdempotencyKey()).isEqualTo("dispatch-key");
        assertThat(job.getStatus()).isEqualTo(MailDispatchJobStatus.REQUESTED);
        assertThat(job.getTargetCount()).isEqualTo(2);
        assertThat(job.getRequestedAt()).isNotNull();
        assertThat(job.getProcessingCount()).isZero();
        assertThat(job.getSuccessCount()).isZero();
        assertThat(job.getFailedCount()).isZero();
    }

    @Test
    @DisplayName("발송 작업은 대상별 처리가 끝나면 성공 건수와 실패 건수를 기록한다")
    void 발송_작업은_대상별_처리가_끝나면_성공_건수와_실패_건수를_기록한다() {
        // given
        MailDispatchJob job = createJob(2);

        // when
        job.startProcessing();
        job.recordSuccess();
        job.recordFailure();

        // then
        assertThat(job.getStatus()).isEqualTo(MailDispatchJobStatus.COMPLETED);
        assertThat(job.getProcessingCount()).isZero();
        assertThat(job.getSuccessCount()).isEqualTo(1);
        assertThat(job.getFailedCount()).isEqualTo(1);
        assertThat(job.getStartedAt()).isNotNull();
        assertThat(job.getFinishedAt()).isNotNull();
    }

    @Test
    @DisplayName("모든 대상 발송에 실패하면 실패 상태가 된다")
    void 모든_대상_발송에_실패하면_실패_상태가_된다() {
        // given
        MailDispatchJob job = createJob(2);

        // when
        job.startProcessing();
        job.recordFailure();
        job.recordFailure();

        // then
        assertThat(job.getStatus()).isEqualTo(MailDispatchJobStatus.FAILED);
        assertThat(job.getFailedCount()).isEqualTo(2);
        assertThat(job.getFinishedAt()).isNotNull();
    }

    @Test
    @DisplayName("처리가 끝난 발송 작업은 다시 처리할 수 없다")
    void 처리가_끝난_발송_작업은_다시_처리할_수_없다() {
        // given
        MailDispatchJob job = createJob(1);
        job.startProcessing();
        job.recordSuccess();

        // when & then
        assertThatThrownBy(job::startProcessing)
                .isInstanceOf(MailException.class);
    }

    @Test
    @DisplayName("대상 건수가 0이면 발송 작업을 생성하지 않는다")
    void 대상_건수가_0이면_발송_작업을_생성하지_않는다() {
        // when & then
        assertThatThrownBy(() -> MailDispatchJob.create(
                1L, 2L, 3L, "dispatch-key", "제목", "본문", "{}", 0))
                .isInstanceOf(MailException.class)
                .extracting("errorCode")
                .isEqualTo(MailErrorCode.INVALID_DISPATCH_TARGET_COUNT);
    }

    @Test
    @DisplayName("대상 건수가 음수이면 발송 작업을 생성하지 않는다")
    void 대상_건수가_음수이면_발송_작업을_생성하지_않는다() {
        // when & then
        assertThatThrownBy(() -> MailDispatchJob.create(
                1L, 2L, 3L, "dispatch-key", "제목", "본문", "{}", -1))
                .isInstanceOf(MailException.class)
                .extracting("errorCode")
                .isEqualTo(MailErrorCode.INVALID_DISPATCH_TARGET_COUNT);
    }

    private MailDispatchJob createJob(int targetCount) {
        return MailDispatchJob.create(1L, 2L, 3L, "dispatch-key", "제목", "본문", "{}", targetCount);
    }
}
