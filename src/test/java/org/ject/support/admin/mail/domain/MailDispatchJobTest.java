package org.ject.support.admin.mail.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.ject.support.admin.mail.exception.MailException;

class MailDispatchJobTest {

    @Test
    void 발송_작업을_생성하면_요청_상태와_대상_건수가_설정된다() {
        // given
        MailDispatchJob job = MailDispatchJob.create(
                1L,
                2L,
                3L,
                "제목 템플릿",
                "본문 템플릿",
                "{}",
                2);

        // when & then
        assertThat(job.getScenarioId()).isEqualTo(1L);
        assertThat(job.getRecruitId()).isEqualTo(2L);
        assertThat(job.getRequestedByAdminId()).isEqualTo(3L);
        assertThat(job.getStatus()).isEqualTo(MailDispatchJobStatus.REQUESTED);
        assertThat(job.getTargetCount()).isEqualTo(2);
        assertThat(job.getProcessingCount()).isZero();
        assertThat(job.getSuccessCount()).isZero();
        assertThat(job.getFailedCount()).isZero();
    }

    @Test
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
    void 처리가_끝난_발송_작업은_다시_처리할_수_없다() {
        // given
        MailDispatchJob job = createJob(1);
        job.startProcessing();
        job.recordSuccess();

        // when & then
        assertThatThrownBy(job::startProcessing)
                .isInstanceOf(MailException.class);
    }

    private MailDispatchJob createJob(int targetCount) {
        return MailDispatchJob.create(1L, 2L, 3L, "제목", "본문", "{}", targetCount);
    }
}
