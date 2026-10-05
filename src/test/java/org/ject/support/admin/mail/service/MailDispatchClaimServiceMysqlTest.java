package org.ject.support.admin.mail.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.ject.support.admin.mail.domain.MailDispatchJob;
import org.ject.support.admin.mail.domain.MailDispatchOutbox;
import org.ject.support.admin.mail.domain.MailDispatchOutboxStatus;
import org.ject.support.admin.mail.domain.MailDispatchJobStatus;
import org.ject.support.admin.mail.domain.MailDispatchTarget;
import org.ject.support.admin.mail.domain.MailDispatchTargetStatus;
import org.ject.support.admin.mail.dto.MailDispatchTargetResponse;
import org.ject.support.admin.mail.exception.MailErrorCode;
import org.ject.support.admin.mail.exception.MailException;
import org.ject.support.admin.mail.repository.MailDispatchJobRepository;
import org.ject.support.admin.mail.repository.MailDispatchOutboxRepository;
import org.ject.support.admin.mail.repository.MailDispatchTargetRepository;
import org.ject.support.base.TestSupport;
import org.ject.support.testconfig.QueryDslTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({MailDispatchClaimService.class, MailDispatchQueryService.class, QueryDslTestConfig.class})
@Testcontainers
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class MailDispatchClaimServiceMysqlTest extends TestSupport {

    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.2");

    @Autowired
    private MailDispatchClaimService claimService;

    @Autowired
    private MailDispatchJobRepository jobRepository;

    @Autowired
    private MailDispatchOutboxRepository outboxRepository;

    @Autowired
    private MailDispatchTargetRepository targetRepository;

    @Autowired
    private MailDispatchQueryService queryService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.driver-class-name", MYSQL::getDriverClassName);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.MySQLDialect");
    }

    @Test
    void 만료된_실행은_재획득하지_않고_불확실_결과로_격리한다() {
        // given
        MailDispatchJob job = jobRepository.saveAndFlush(MailDispatchJob.create(
                1L, 2L, 3L, UUID.randomUUID().toString(), "제목", "본문", "{}", 1));
        targetRepository.saveAndFlush(MailDispatchTarget.pending(job, 10L, "applicant@ject.kr"));
        MailDispatchOutbox outbox = outboxRepository.saveAndFlush(MailDispatchOutbox.createPending(
                job, 10L, "applicant@ject.kr", "제목", "본문"));
        LocalDateTime now = LocalDateTime.of(2026, 10, 2, 12, 0);
        var claim = claimService.claim(outbox.getId(), now, Duration.ofSeconds(30)).orElseThrow();

        // when
        assertThat(claimService.claim(outbox.getId(), now.plusSeconds(31), Duration.ofSeconds(30)))
                .isEmpty();
        assertThat(claimService.quarantineExpired(outbox.getId(), now.plusSeconds(30))).isTrue();

        // then
        assertThat(outboxRepository.findById(outbox.getId())).get().satisfies(saved -> {
            assertThat(saved.getStatus()).isEqualTo(MailDispatchOutboxStatus.UNKNOWN);
            assertThat(saved.getClaimToken()).isEqualTo(claim.claimToken());
        });
        assertThat(claimService.claim(outbox.getId(), now.plusSeconds(60), Duration.ofSeconds(30)))
                .isEmpty();
    }

    @Test
    void 동시_실행자_중_한_실행자만_같은_대상을_획득한다() throws Exception {
        // given
        MailDispatchJob job = jobRepository.saveAndFlush(MailDispatchJob.create(
                1L, 2L, 3L, UUID.randomUUID().toString(), "제목", "본문", "{}", 1));
        targetRepository.saveAndFlush(MailDispatchTarget.pending(job, 10L, "applicant@ject.kr"));
        MailDispatchOutbox outbox = outboxRepository.saveAndFlush(MailDispatchOutbox.createPending(
                job, 10L, "applicant@ject.kr", "제목", "본문"));
        LocalDateTime now = LocalDateTime.of(2026, 10, 2, 12, 0);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try (var executor = Executors.newFixedThreadPool(2)) {
            Callable<Boolean> attempt = () -> {
                ready.countDown();
                if (!start.await(10, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("claim start timed out");
                }
                return claimService.claim(outbox.getId(), now, Duration.ofSeconds(30)).isPresent();
            };
            var first = executor.submit(attempt);
            var second = executor.submit(attempt);
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();

            // when
            start.countDown();
            boolean firstClaimed = first.get(10, TimeUnit.SECONDS);
            boolean secondClaimed = second.get(10, TimeUnit.SECONDS);

            // then
            assertThat(firstClaimed ^ secondClaimed).isTrue();
            assertThat(outboxRepository.findById(outbox.getId())).get()
                    .extracting(MailDispatchOutbox::getStatus)
                    .isEqualTo(MailDispatchOutboxStatus.PROCESSING);
        } finally {
            start.countDown();
        }
    }

    @Test
    void 유효한_lease는_격리하지_않고_기존_결과_경로로_덮어쓸_수_없다() {
        // given
        MailDispatchJob job = jobRepository.saveAndFlush(MailDispatchJob.create(
                1L, 2L, 3L, UUID.randomUUID().toString(), "제목", "본문", "{}", 1));
        targetRepository.saveAndFlush(MailDispatchTarget.pending(job, 10L, "applicant@ject.kr"));
        MailDispatchOutbox outbox = outboxRepository.saveAndFlush(MailDispatchOutbox.createPending(
                job, 10L, "applicant@ject.kr", "제목", "본문"));
        LocalDateTime now = LocalDateTime.of(2026, 10, 2, 12, 0);
        claimService.claim(outbox.getId(), now, Duration.ofSeconds(30)).orElseThrow();

        // when & then
        assertThat(claimService.quarantineExpired(outbox.getId(), now.plusSeconds(29))).isFalse();
        MailDispatchOutbox saved = outboxRepository.findById(outbox.getId()).orElseThrow();
        assertThatThrownBy(saved::markSent).isInstanceOf(MailException.class);
        assertThat(saved.getStatus()).isEqualTo(MailDispatchOutboxStatus.PROCESSING);
    }

    @Test
    void 만료된_결과는_성공으로_덮어쓰지_않고_관리자에게_불확실_건수를_제공한다() {
        // given
        MailDispatchJob job = jobRepository.saveAndFlush(MailDispatchJob.create(
                1L, 2L, 3L, UUID.randomUUID().toString(), "제목", "본문", "{}", 2));
        targetRepository.saveAndFlush(MailDispatchTarget.pending(job, 10L, "first@ject.kr"));
        targetRepository.saveAndFlush(MailDispatchTarget.pending(job, 20L, "second@ject.kr"));
        MailDispatchOutbox first = outboxRepository.saveAndFlush(MailDispatchOutbox.createPending(
                job, 10L, "first@ject.kr", "제목", "본문"));
        MailDispatchOutbox second = outboxRepository.saveAndFlush(MailDispatchOutbox.createPending(
                job, 20L, "second@ject.kr", "제목", "본문"));
        LocalDateTime now = LocalDateTime.of(2026, 10, 2, 12, 0);
        var firstClaim = claimService.claim(first.getId(), now, Duration.ofSeconds(30)).orElseThrow();
        var secondClaim = claimService.claim(second.getId(), now, Duration.ofSeconds(30)).orElseThrow();

        // when
        assertThat(claimService.recordSuccess(first.getId(), "wrong-token", now.plusSeconds(1))).isFalse();
        assertThat(claimService.recordSuccess(first.getId(), firstClaim.claimToken(), now.plusSeconds(1)))
                .isTrue();
        assertThat(claimService.recordSuccess(second.getId(), secondClaim.claimToken(), now.plusSeconds(30)))
                .isFalse();
        assertThat(claimService.recordSuccess(second.getId(), secondClaim.claimToken(), now.plusSeconds(31)))
                .isFalse();

        // then
        var result = queryService.getJob(3L, job.getId());
        assertThat(result.status()).isEqualTo(MailDispatchJobStatus.UNKNOWN);
        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.unknownCount()).isEqualTo(1);
        assertThat(result.failedCount()).isZero();
        assertThat(result.processingCount()).isZero();
        assertThat(queryService.searchTargets(3L, job.getId(), MailDispatchTargetStatus.UNKNOWN,
                PageRequest.of(0, 10)).getContent()).singleElement().satisfies(target -> {
                    assertThat(target.applyId()).isEqualTo(20L);
                    assertThat(target.failureReason()).isEqualTo("MAIL-21");
                    assertThat(target.sentAt()).isNull();
                });
    }

    @Test
    void 유효한_claim의_확정_실패는_안전한_코드로_한_번만_집계한다() {
        // given
        MailDispatchJob job = jobRepository.saveAndFlush(MailDispatchJob.create(
                1L, 2L, 3L, UUID.randomUUID().toString(), "제목", "본문", "{}", 1));
        targetRepository.saveAndFlush(MailDispatchTarget.pending(job, 10L, "applicant@ject.kr"));
        MailDispatchOutbox outbox = outboxRepository.saveAndFlush(MailDispatchOutbox.createPending(
                job, 10L, "applicant@ject.kr", "제목", "본문"));
        LocalDateTime now = LocalDateTime.of(2026, 10, 2, 12, 0);
        var claim = claimService.claim(outbox.getId(), now, Duration.ofSeconds(30)).orElseThrow();

        // when
        assertThat(claimService.recordFailure(outbox.getId(), claim.claimToken(), now.plusSeconds(1),
                MailErrorCode.MAIL_SEND_FAILURE)).isTrue();
        assertThat(claimService.recordFailure(outbox.getId(), claim.claimToken(), now.plusSeconds(2),
                MailErrorCode.MAIL_SEND_FAILURE)).isFalse();

        // then
        var result = queryService.getJob(3L, job.getId());
        assertThat(result.status()).isEqualTo(MailDispatchJobStatus.FAILED);
        assertThat(result.failedCount()).isEqualTo(1);
        assertThat(result.unknownCount()).isZero();
        assertThat(queryService.searchTargets(3L, job.getId(), MailDispatchTargetStatus.FAILED,
                PageRequest.of(0, 10)).getContent()).singleElement()
                .extracting(MailDispatchTargetResponse::failureReason)
                .isEqualTo("MAIL-19");
    }

    @Test
    void 결과_DB_기록_실패는_집계를_롤백하고_lease_만료_후_격리한다() {
        // given
        MailDispatchJob job = jobRepository.saveAndFlush(MailDispatchJob.create(
                1L, 2L, 3L, UUID.randomUUID().toString(), "제목", "본문", "{}", 1));
        targetRepository.saveAndFlush(MailDispatchTarget.pending(job, 91L, "applicant@ject.kr"));
        MailDispatchOutbox outbox = outboxRepository.saveAndFlush(MailDispatchOutbox.createPending(
                job, 91L, "applicant@ject.kr", "제목", "본문"));
        LocalDateTime now = LocalDateTime.of(2026, 10, 2, 12, 0);
        var claim = claimService.claim(outbox.getId(), now, Duration.ofSeconds(30)).orElseThrow();
        jdbcTemplate.execute("""
                ALTER TABLE mail_dispatch_outbox
                ADD CONSTRAINT simulated_result_failure CHECK (apply_id <> 91 OR status <> 'SENT')
                """);

        try {
            // when
            assertThatThrownBy(() -> claimService.recordSuccess(
                    outbox.getId(), claim.claimToken(), now.plusSeconds(1)))
                    .isInstanceOf(RuntimeException.class)
                    .hasRootCauseMessage("Check constraint 'simulated_result_failure' is violated.");

            // then
            var result = queryService.getJob(3L, job.getId());
            assertThat(result.processingCount()).isEqualTo(1);
            assertThat(result.successCount()).isZero();
            assertThat(result.unknownCount()).isZero();
            assertThat(queryService.searchTargets(3L, job.getId(), MailDispatchTargetStatus.PENDING,
                    PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);
            assertThat(outboxRepository.findById(outbox.getId())).get()
                    .extracting(MailDispatchOutbox::getStatus)
                    .isEqualTo(MailDispatchOutboxStatus.PROCESSING);
        } finally {
            jdbcTemplate.execute("ALTER TABLE mail_dispatch_outbox DROP CHECK simulated_result_failure");
        }
        assertThat(claimService.quarantineExpired(outbox.getId(), now.plusSeconds(30))).isTrue();
        assertThat(queryService.getJob(3L, job.getId()).unknownCount()).isEqualTo(1);
    }

    @Test
    void 기존_동기_실행의_미확정_대상은_획득하지_않고_격리한다() {
        // given
        MailDispatchJob job = MailDispatchJob.create(
                1L, 2L, 3L, UUID.randomUUID().toString(), "제목", "본문", "{}", 1);
        job.startProcessing();
        job = jobRepository.saveAndFlush(job);
        targetRepository.saveAndFlush(MailDispatchTarget.pending(job, 10L, "applicant@ject.kr"));
        MailDispatchOutbox outbox = outboxRepository.saveAndFlush(MailDispatchOutbox.createPending(
                job, 10L, "applicant@ject.kr", "제목", "본문"));

        // when
        assertThat(claimService.claim(outbox.getId(), LocalDateTime.of(2026, 10, 2, 12, 0),
                Duration.ofSeconds(30))).isEmpty();

        // then
        var result = queryService.getJob(3L, job.getId());
        assertThat(result.status()).isEqualTo(MailDispatchJobStatus.UNKNOWN);
        assertThat(result.unknownCount()).isEqualTo(1);
        assertThat(result.processingCount()).isZero();
    }

    @Test
    void 서로_다른_대상의_동시_성공_기록은_작업_집계를_잃지_않는다() throws Exception {
        // given
        MailDispatchJob job = jobRepository.saveAndFlush(MailDispatchJob.create(
                1L, 2L, 3L, UUID.randomUUID().toString(), "제목", "본문", "{}", 2));
        targetRepository.saveAndFlush(MailDispatchTarget.pending(job, 10L, "first@ject.kr"));
        targetRepository.saveAndFlush(MailDispatchTarget.pending(job, 20L, "second@ject.kr"));
        MailDispatchOutbox first = outboxRepository.saveAndFlush(MailDispatchOutbox.createPending(
                job, 10L, "first@ject.kr", "제목", "본문"));
        MailDispatchOutbox second = outboxRepository.saveAndFlush(MailDispatchOutbox.createPending(
                job, 20L, "second@ject.kr", "제목", "본문"));
        LocalDateTime now = LocalDateTime.of(2026, 10, 2, 12, 0);
        var firstClaim = claimService.claim(first.getId(), now, Duration.ofSeconds(30)).orElseThrow();
        var secondClaim = claimService.claim(second.getId(), now, Duration.ofSeconds(30)).orElseThrow();
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var firstResult = executor.submit(() -> {
                if (!start.await(10, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("result start timed out");
                }
                return claimService.recordSuccess(first.getId(), firstClaim.claimToken(), now.plusSeconds(1));
            });
            var secondResult = executor.submit(() -> {
                if (!start.await(10, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("result start timed out");
                }
                return claimService.recordSuccess(second.getId(), secondClaim.claimToken(), now.plusSeconds(1));
            });

            // when
            start.countDown();
            assertThat(firstResult.get(10, TimeUnit.SECONDS)).isTrue();
            assertThat(secondResult.get(10, TimeUnit.SECONDS)).isTrue();

            // then
            var result = queryService.getJob(3L, job.getId());
            assertThat(result.status()).isEqualTo(MailDispatchJobStatus.COMPLETED);
            assertThat(result.successCount()).isEqualTo(2);
            assertThat(result.processingCount()).isZero();
            assertThat(result.unknownCount()).isZero();
        } finally {
            start.countDown();
        }
    }
}
