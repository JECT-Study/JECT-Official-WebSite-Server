package org.ject.support.admin.mail.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.ject.support.admin.mail.domain.MailDispatchJobStatus;
import org.ject.support.admin.mail.repository.MailDispatchJobRepository;
import org.ject.support.admin.mail.repository.MailDispatchOutboxRepository;
import org.ject.support.admin.mail.repository.MailDispatchTargetRepository;
import org.ject.support.base.TestSupport;
import org.ject.support.common.response.ObjectMapperConfig;
import org.ject.support.common.util.Map2JsonSerializer;
import org.ject.support.testconfig.QueryDslTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
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
@Import({MailDispatchPersistenceService.class, MailDispatchClaimService.class, MailDispatchQueryService.class,
        MailDispatchTransitionTelemetryService.class,
        QueryDslTestConfig.class, ObjectMapperConfig.class, Map2JsonSerializer.class,
        MailDispatchTransitionMetricsMysqlTest.MetricsDependencies.class})
@Testcontainers
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@ExtendWith(OutputCaptureExtension.class)
class MailDispatchTransitionMetricsMysqlTest extends TestSupport {

    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.2");

    @Autowired
    private MailDispatchPersistenceService persistenceService;

    @Autowired
    private MailDispatchClaimService claimService;

    @Autowired
    private MailDispatchQueryService queryService;

    @Autowired
    private MailDispatchJobRepository jobRepository;

    @Autowired
    private MailDispatchOutboxRepository outboxRepository;

    @Autowired
    private MailDispatchTargetRepository targetRepository;

    @Autowired
    private SimpleMeterRegistry meterRegistry;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AtomicBoolean meterFailure;

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.driver-class-name", MYSQL::getDriverClassName);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.MySQLDialect");
    }

    @BeforeEach
    void 발송_기록과_계측값_정리() {
        meterFailure.set(false);
        outboxRepository.deleteAllInBatch();
        targetRepository.deleteAllInBatch();
        jobRepository.deleteAllInBatch();
        meterRegistry.clear();
    }

    @Test
    void 커밋된_발송_상태만_집계하고_로그와_태그에_민감정보를_남기지_않는다(CapturedOutput output) {
        // given
        var job = persistenceService.createJob(plan(), "fixture");
        var outbox = outboxRepository.findByDispatchJobIdAndApplyId(job.getId(), 10L).orElseThrow();
        LocalDateTime now = LocalDateTime.of(2026, 10, 4, 12, 0);

        // when
        var claim = claimService.claim(outbox.getId(), now, Duration.ofMinutes(2)).orElseThrow();
        assertThat(count("PROCESSING")).isEqualTo(1);
        assertThat(claimService.recordSuccess(outbox.getId(), claim.claimToken(), now.plusSeconds(1))).isTrue();

        // then
        assertThat(queryService.getJob(3L, job.getId()).status()).isEqualTo(MailDispatchJobStatus.COMPLETED);
        assertThat(count("SENT")).isEqualTo(1);
        assertThat(claimService.recordSuccess(outbox.getId(), claim.claimToken(), now.plusSeconds(2))).isFalse();
        assertThat(count("SENT")).isEqualTo(1);
        assertThat(meterRegistry.getMeters()).allSatisfy(meter ->
                assertThat(meter.getId().getTags()).extracting(Tag::getKey).containsExactly("status"));
        assertThat(output.getAll()).contains("[MAIL_DISPATCH_STATE]", "jobId=" + job.getId(), "status=SENT")
                .doesNotContain("private@example.com", "비공개 제목", "비공개 본문", claim.claimToken());
    }

    @Test
    void 재시도_대기와_최종_실패를_서로_다른_상태로_집계한다() {
        // given
        var job = persistenceService.createJob(plan(), "fixture");
        var outbox = outboxRepository.findByDispatchJobIdAndApplyId(job.getId(), 10L).orElseThrow();
        LocalDateTime now = LocalDateTime.of(2026, 10, 4, 12, 0);

        // when
        var first = claimService.claim(outbox.getId(), now, Duration.ofMinutes(2)).orElseThrow();
        claimService.recordThrottling(outbox.getId(), first.claimToken(), now.plusSeconds(1));

        // then
        assertThat(count("PENDING")).isEqualTo(1);
        assertThat(count("FAILED")).isZero();
        var second = claimService.claim(outbox.getId(), now.plusSeconds(31), Duration.ofMinutes(2)).orElseThrow();
        claimService.recordThrottling(outbox.getId(), second.claimToken(), now.plusSeconds(32));
        var third = claimService.claim(outbox.getId(), now.plusSeconds(92), Duration.ofMinutes(2)).orElseThrow();
        claimService.recordThrottling(outbox.getId(), third.claimToken(), now.plusSeconds(93));
        assertThat(count("PROCESSING")).isEqualTo(3);
        assertThat(count("PENDING")).isEqualTo(2);
        assertThat(count("FAILED")).isEqualTo(1);
        assertThat(queryService.getJob(3L, job.getId()).failedCount()).isEqualTo(1);
    }

    @Test
    void 불확실_결과와_만료된_발송을_확인_필요_상태로_한_번씩_집계한다() {
        // given
        var job = persistenceService.createJob(plan(), "fixture");
        var outbox = outboxRepository.findByDispatchJobIdAndApplyId(job.getId(), 10L).orElseThrow();
        LocalDateTime now = LocalDateTime.of(2026, 10, 4, 12, 0);
        var claim = claimService.claim(outbox.getId(), now, Duration.ofMinutes(2)).orElseThrow();

        // when
        assertThat(claimService.recordUnknown(outbox.getId(), claim.claimToken())).isTrue();

        // then
        assertThat(queryService.getJob(3L, job.getId()).unknownCount()).isEqualTo(1);
        assertThat(count("UNKNOWN")).isEqualTo(1);
        assertThat(claimService.recordUnknown(outbox.getId(), claim.claimToken())).isFalse();
        assertThat(count("UNKNOWN")).isEqualTo(1);

        var expiredJob = persistenceService.createJob(plan(), "fixture");
        var expiredOutbox = outboxRepository.findByDispatchJobIdAndApplyId(expiredJob.getId(), 10L).orElseThrow();
        var expiredClaim = claimService.claim(expiredOutbox.getId(), now, Duration.ofMinutes(2)).orElseThrow();
        assertThat(claimService.recordSuccess(
                expiredOutbox.getId(), expiredClaim.claimToken(), now.plusSeconds(121))).isFalse();
        assertThat(queryService.getJob(3L, expiredJob.getId()).unknownCount()).isEqualTo(1);
        assertThat(count("UNKNOWN")).isEqualTo(2);
        assertThat(count("SENT")).isZero();
    }

    @Test
    void 결과_저장이_롤백되면_성공_로그와_숫자를_남기지_않는다(CapturedOutput output) {
        // given
        var job = persistenceService.createJob(plan(), "fixture");
        var outbox = outboxRepository.findByDispatchJobIdAndApplyId(job.getId(), 10L).orElseThrow();
        LocalDateTime now = LocalDateTime.of(2026, 10, 4, 12, 0);
        var claim = claimService.claim(outbox.getId(), now, Duration.ofMinutes(2)).orElseThrow();
        jdbcTemplate.execute("""
                ALTER TABLE mail_dispatch_outbox
                ADD CONSTRAINT simulated_telemetry_result_failure CHECK (status <> 'SENT')
                """);

        try {
            // when
            assertThatThrownBy(() -> claimService.recordSuccess(
                    outbox.getId(), claim.claimToken(), now.plusSeconds(1)))
                    .isInstanceOf(RuntimeException.class)
                    .hasRootCauseMessage("Check constraint 'simulated_telemetry_result_failure' is violated.");

            // then
            var result = queryService.getJob(3L, job.getId());
            assertThat(result.processingCount()).isEqualTo(1);
            assertThat(result.successCount()).isZero();
            assertThat(count("PROCESSING")).isEqualTo(1);
            assertThat(count("SENT")).isZero();
            assertThat(output.getAll().lines().filter(line -> line.contains("[MAIL_DISPATCH_STATE]")).toList())
                    .noneMatch(line -> line.contains("status=SENT"));
        } finally {
            jdbcTemplate.execute("ALTER TABLE mail_dispatch_outbox DROP CHECK simulated_telemetry_result_failure");
        }
    }

    @Test
    void 계측_장애가_발송_결과를_바꾸거나_민감한_오류를_노출하지_않는다(CapturedOutput output) {
        // given
        var job = persistenceService.createJob(plan(), "fixture");
        var outbox = outboxRepository.findByDispatchJobIdAndApplyId(job.getId(), 10L).orElseThrow();
        LocalDateTime now = LocalDateTime.of(2026, 10, 4, 12, 0);
        var claim = claimService.claim(outbox.getId(), now, Duration.ofMinutes(2)).orElseThrow();
        meterFailure.set(true);

        // when
        assertThat(claimService.recordSuccess(outbox.getId(), claim.claimToken(), now.plusSeconds(1))).isTrue();

        // then
        var result = queryService.getJob(3L, job.getId());
        assertThat(result.status()).isEqualTo(MailDispatchJobStatus.COMPLETED);
        assertThat(result.successCount()).isEqualTo(1);
        assertThat(output.getAll()).contains("[MAIL_DISPATCH_TELEMETRY_FAILURE]", "errorType=IllegalStateException")
                .doesNotContain("SECRET_METRIC_MESSAGE", "private@example.com", "비공개 제목", "비공개 본문",
                        claim.claimToken());
    }

    private double count(String status) {
        var counter = meterRegistry.find("mail.dispatch.transitions").tag("status", status).counter();
        return counter == null ? 0 : counter.count();
    }

    private MailDispatchPlan plan() {
        return new MailDispatchPlan(1L, 2L, 3L, UUID.randomUUID().toString(), "템플릿 제목", "템플릿 본문", Map.of(),
                List.of(new MailDispatchPlan.Target(10L, "private@example.com", "비공개 제목", "비공개 본문")));
    }

    @TestConfiguration
    static class MetricsDependencies {

        @Bean
        AtomicBoolean meterFailure() {
            return new AtomicBoolean();
        }

        @Bean
        SimpleMeterRegistry meterRegistry(AtomicBoolean meterFailure) {
            var registry = new SimpleMeterRegistry();
            registry.config().meterFilter(new MeterFilter() {
                @Override
                public Meter.Id map(Meter.Id id) {
                    if (meterFailure.get() && "SENT".equals(id.getTag("status"))) {
                        throw new IllegalStateException("SECRET_METRIC_MESSAGE");
                    }
                    return id;
                }
            });
            return registry;
        }
    }
}
