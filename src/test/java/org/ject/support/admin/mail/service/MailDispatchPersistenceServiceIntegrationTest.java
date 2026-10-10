package org.ject.support.admin.mail.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import org.ject.support.admin.mail.domain.MailDispatchJob;
import org.ject.support.admin.mail.domain.MailDispatchJobStatus;
import org.ject.support.admin.mail.domain.MailDispatchOutbox;
import org.ject.support.admin.mail.domain.MailDispatchOutboxStatus;
import org.ject.support.admin.mail.domain.MailDispatchTarget;
import org.ject.support.admin.mail.domain.MailDispatchTargetStatus;
import org.ject.support.admin.mail.repository.MailDispatchJobRepository;
import org.ject.support.admin.mail.repository.MailDispatchOutboxRepository;
import org.ject.support.admin.mail.repository.MailDispatchTargetRepository;
import org.ject.support.base.TestSupport;
import org.ject.support.common.util.Map2JsonSerializer;
import org.ject.support.testconfig.QueryDslTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Import({
        QueryDslTestConfig.class,
        MailDispatchPersistenceService.class,
        Map2JsonSerializer.class
})
@ImportAutoConfiguration(JacksonAutoConfiguration.class)
@DataJpaTest
class MailDispatchPersistenceServiceIntegrationTest extends TestSupport {

    @Autowired
    private MailDispatchPersistenceService mailDispatchPersistenceService;

    @Autowired
    private MailDispatchJobRepository mailDispatchJobRepository;

    @Autowired
    private MailDispatchTargetRepository mailDispatchTargetRepository;

    @Autowired
    private MailDispatchOutboxRepository mailDispatchOutboxRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("발송 작업 생성 시 수신자별 렌더링 결과를 Outbox snapshot으로 저장한다")
    void 발송_작업_생성_시_수신자별_렌더링_결과를_Outbox_snapshot으로_저장한다() {
        // given
        MailDispatchPlan plan = createPlan("dispatch-key");

        // when
        var savedJob = mailDispatchPersistenceService.createJob(plan, "fingerprint");
        entityManager.flush();
        entityManager.clear();

        // then
        var persistedJob = mailDispatchJobRepository.findById(savedJob.getId()).orElseThrow();
        MailDispatchTarget target = mailDispatchTargetRepository
                .findByDispatchJobIdAndApplyId(persistedJob.getId(), 10L)
                .orElseThrow();
        List<MailDispatchOutbox> outboxes = mailDispatchOutboxRepository
                .findAllByDispatchJobIdOrderByIdAsc(persistedJob.getId());

        assertThat(target.getEmail()).isEqualTo("applicant@ject.kr");
        assertThat(outboxes).singleElement().satisfies(outbox -> {
            assertThat(outbox.getDispatchJob().getId()).isEqualTo(persistedJob.getId());
            assertThat(outbox.getApplyId()).isEqualTo(10L);
            assertThat(outbox.getEmail()).isEqualTo("applicant@ject.kr");
            assertThat(outbox.getSubject()).isEqualTo("렌더링된 제목");
            assertThat(outbox.getBody()).isEqualTo("렌더링된 본문");
            assertThat(outbox.getStatus()).isEqualTo(MailDispatchOutboxStatus.PENDING);
        });
    }

    @Test
    @DisplayName("대용량 렌더링 본문도 Outbox snapshot으로 보존한다")
    void 대용량_본문도_Outbox에_보존한다() {
        // given
        String body = "a".repeat(65_536);

        // when
        var savedJob = mailDispatchPersistenceService.createJob(createPlan("large-body-key", body), "fingerprint");
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(mailDispatchOutboxRepository.findByDispatchJobIdAndApplyId(savedJob.getId(), 10L).orElseThrow()
                .getBody()).isEqualTo(body);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @DisplayName("Outbox 저장에 실패하면 발송 작업과 대상도 함께 롤백한다")
    void Outbox_저장에_실패하면_발송_작업과_대상도_함께_롤백한다() {
        // given
        jdbcTemplate.execute("ALTER TABLE mail_dispatch_outbox ADD CONSTRAINT chk_mail_dispatch_outbox_pending_test "
                + "CHECK (status <> 'PENDING')");

        try {
            // when & then
            assertThatThrownBy(() -> mailDispatchPersistenceService.createJob(createPlan("atomicity-key"), "fingerprint"))
                    .isInstanceOf(DataIntegrityViolationException.class);
            assertThat(mailDispatchJobRepository.count()).isZero();
            assertThat(mailDispatchTargetRepository.count()).isZero();
            assertThat(mailDispatchOutboxRepository.count()).isZero();
        } finally {
            jdbcTemplate.execute("ALTER TABLE mail_dispatch_outbox "
                    + "DROP CONSTRAINT chk_mail_dispatch_outbox_pending_test");
        }
    }

    @Test
    @DisplayName("수신자 발송 성공을 기록하면 Outbox도 성공 상태로 변경한다")
    void 수신자_발송_성공을_기록하면_Outbox도_성공_상태로_변경한다() {
        // given
        var job = mailDispatchPersistenceService.createJob(createPlan("success-key"), "fingerprint");
        mailDispatchPersistenceService.startProcessing(job.getId());

        // when
        mailDispatchPersistenceService.recordSuccess(job.getId(), 10L);
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(mailDispatchTargetRepository.findByDispatchJobIdAndApplyId(job.getId(), 10L).orElseThrow()
                .getStatus()).isEqualTo(MailDispatchTargetStatus.SENT);
        assertThat(mailDispatchOutboxRepository.findByDispatchJobIdAndApplyId(job.getId(), 10L).orElseThrow()
                .getStatus()).isEqualTo(MailDispatchOutboxStatus.SENT);
        assertThat(mailDispatchOutboxRepository.findByDispatchJobIdAndApplyId(job.getId(), 10L).orElseThrow()
                .getFailureReason()).isNull();
    }

    @Test
    @DisplayName("수신자 발송 실패를 기록하면 Outbox도 실패 상태와 사유를 저장한다")
    void 수신자_발송_실패를_기록하면_Outbox도_실패_상태와_사유를_저장한다() {
        // given
        var job = mailDispatchPersistenceService.createJob(createPlan("failure-key"), "fingerprint");
        mailDispatchPersistenceService.startProcessing(job.getId());

        // when
        mailDispatchPersistenceService.recordFailure(job.getId(), 10L, "MAIL-19");
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(mailDispatchTargetRepository.findByDispatchJobIdAndApplyId(job.getId(), 10L).orElseThrow()
                .getStatus()).isEqualTo(MailDispatchTargetStatus.FAILED);
        assertThat(mailDispatchOutboxRepository.findByDispatchJobIdAndApplyId(job.getId(), 10L).orElseThrow()
                .getStatus()).isEqualTo(MailDispatchOutboxStatus.FAILED);
        assertThat(mailDispatchOutboxRepository.findByDispatchJobIdAndApplyId(job.getId(), 10L).orElseThrow()
                .getFailureReason()).isEqualTo("MAIL-19");
    }

    @Test
    @DisplayName("Outbox가 없는 기존 작업의 발송 결과도 기록한다")
    void Outbox가_없는_기존_작업의_발송_결과도_기록한다() {
        // given
        MailDispatchJob job = mailDispatchJobRepository.saveAndFlush(
                MailDispatchJob.create(1L, 2L, 3L, "legacy-key", "제목", "본문", "{}", 2));
        mailDispatchTargetRepository.saveAllAndFlush(List.of(
                MailDispatchTarget.pending(job, 10L, "sent@ject.kr"),
                MailDispatchTarget.pending(job, 11L, "failed@ject.kr")));
        mailDispatchPersistenceService.startProcessing(job.getId());

        // when
        mailDispatchPersistenceService.recordSuccess(job.getId(), 10L);
        mailDispatchPersistenceService.recordFailure(job.getId(), 11L, "MAIL-19");
        entityManager.flush();
        entityManager.clear();

        // then
        MailDispatchJob persistedJob = mailDispatchJobRepository.findById(job.getId()).orElseThrow();
        assertThat(persistedJob.getStatus()).isEqualTo(MailDispatchJobStatus.COMPLETED);
        assertThat(persistedJob.getSuccessCount()).isEqualTo(1);
        assertThat(persistedJob.getFailedCount()).isEqualTo(1);
        assertThat(mailDispatchTargetRepository.findByDispatchJobIdAndApplyId(job.getId(), 10L).orElseThrow()
                .getStatus()).isEqualTo(MailDispatchTargetStatus.SENT);
        assertThat(mailDispatchTargetRepository.findByDispatchJobIdAndApplyId(job.getId(), 11L).orElseThrow()
                .getStatus()).isEqualTo(MailDispatchTargetStatus.FAILED);
        assertThat(mailDispatchOutboxRepository.findAllByDispatchJobIdOrderByIdAsc(job.getId())).isEmpty();
    }

    private MailDispatchPlan createPlan(String idempotencyKey) {
        return createPlan(idempotencyKey, "렌더링된 본문");
    }

    private MailDispatchPlan createPlan(String idempotencyKey, String body) {
        return new MailDispatchPlan(
                1L,
                2L,
                3L,
                idempotencyKey,
                "제목 템플릿",
                "본문 템플릿",
                Map.of("MESSAGE", "안내"),
                List.of(new MailDispatchPlan.Target(10L, "applicant@ject.kr", "렌더링된 제목", body)));
    }
}
