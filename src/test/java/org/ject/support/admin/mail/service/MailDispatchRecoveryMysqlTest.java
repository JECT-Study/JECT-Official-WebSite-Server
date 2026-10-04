package org.ject.support.admin.mail.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.ject.support.admin.mail.domain.MailDispatchJobStatus;
import org.ject.support.admin.mail.domain.MailDispatchTargetStatus;
import org.ject.support.admin.mail.repository.MailDispatchJobRepository;
import org.ject.support.admin.mail.repository.MailDispatchOutboxRepository;
import org.ject.support.admin.mail.repository.MailDispatchTargetRepository;
import org.ject.support.base.TestSupport;
import org.ject.support.common.util.Map2JsonSerializer;
import org.ject.support.external.email.service.EmailSendService;
import org.ject.support.testconfig.QueryDslTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({MailDispatchPersistenceService.class, MailDispatchClaimService.class,
        MailDispatchExecutionService.class, MailDispatchQueryService.class,
        QueryDslTestConfig.class, MailDispatchRecoveryMysqlTest.TestDependencies.class})
@Testcontainers
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class MailDispatchRecoveryMysqlTest extends TestSupport {

    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.2");

    @Autowired
    private MailDispatchPersistenceService persistenceService;

    @Autowired
    private MailDispatchClaimService claimService;

    @Autowired
    private MailDispatchExecutionService executionService;

    @Autowired
    private MailDispatchQueryService queryService;

    @Autowired
    private MailDispatchJobRepository jobRepository;

    @Autowired
    private MailDispatchOutboxRepository outboxRepository;

    @Autowired
    private MailDispatchTargetRepository targetRepository;

    @MockitoBean
    private EmailSendService emailSendService;

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.driver-class-name", MYSQL::getDriverClassName);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.MySQLDialect");
    }

    @BeforeEach
    void 발송_테스트_데이터_정리() {
        outboxRepository.deleteAllInBatch();
        targetRepository.deleteAllInBatch();
        jobRepository.deleteAllInBatch();
    }

    @Test
    void 실행_중인_대상이_앞에_있어도_미처리_대상을_제한된_개수만_발송한다() {
        // given
        var job = persistenceService.createJob(plan(10L, 20L, 30L), "fixture");
        var active = outboxRepository.findByDispatchJobIdAndApplyId(job.getId(), 10L).orElseThrow();
        claimService.claim(active.getId(), LocalDateTime.now(), Duration.ofDays(1)).orElseThrow();

        // when
        executionService.executePendingBatch(1);

        // then
        var result = queryService.getJob(3L, job.getId());
        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.processingCount()).isEqualTo(2);
        assertThat(queryService.searchTargets(3L, job.getId(), MailDispatchTargetStatus.SENT,
                PageRequest.of(0, 10)).getContent()).singleElement()
                .satisfies(target -> assertThat(target.applyId()).isEqualTo(20L));
        verify(emailSendService).sendEmail("20@ject.kr", "저장된 제목", "저장된 본문");
        executionService.executePendingBatch(1);
        executionService.executePendingBatch(1);
        var nextResult = queryService.getJob(3L, job.getId());
        assertThat(nextResult.successCount()).isEqualTo(2);
        assertThat(nextResult.processingCount()).isEqualTo(1);
        verify(emailSendService).sendEmail("30@ject.kr", "저장된 제목", "저장된 본문");
        verifyNoMoreInteractions(emailSendService);
    }

    @Test
    void 만료된_대상만_격리하고_미처리_대상은_발송하되_유효한_실행은_보존한다() {
        // given
        var job = persistenceService.createJob(plan(10L, 20L, 30L), "fixture");
        var active = outboxRepository.findByDispatchJobIdAndApplyId(job.getId(), 10L).orElseThrow();
        var expired = outboxRepository.findByDispatchJobIdAndApplyId(job.getId(), 20L).orElseThrow();
        LocalDateTime now = LocalDateTime.now().withNano(0);
        claimService.claim(active.getId(), now, Duration.ofDays(1)).orElseThrow();
        var expiredClaim = claimService.claim(expired.getId(), now.minusMinutes(2), Duration.ofMinutes(2))
                .orElseThrow();

        // when
        assertThat(executionService.quarantineInterruptedBatch(now, 1)).isEqualTo(1);
        executionService.executePendingBatch(10);

        // then
        var result = queryService.getJob(3L, job.getId());
        assertThat(result.unknownCount()).isEqualTo(1);
        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.processingCount()).isEqualTo(1);
        assertThat(claimService.recordSuccess(expired.getId(), expiredClaim.claimToken(), now)).isFalse();
        assertThat(executionService.quarantineInterruptedBatch(now, 1)).isZero();
        assertThat(queryService.searchTargets(3L, job.getId(), MailDispatchTargetStatus.UNKNOWN,
                PageRequest.of(0, 10)).getContent()).singleElement().satisfies(target -> {
                    assertThat(target.applyId()).isEqualTo(20L);
                    assertThat(target.failureReason()).isEqualTo("MAIL-21");
                    assertThat(target.sentAt()).isNull();
                });
        verify(emailSendService).sendEmail("30@ject.kr", "저장된 제목", "저장된 본문");
        verifyNoMoreInteractions(emailSendService);
    }

    @Test
    void 기존_동기_실행의_미확정_대상은_발송하지_않고_배치_한도만큼_격리한다() {
        // given
        var job = persistenceService.createJob(plan(10L, 20L), "fixture");
        persistenceService.startProcessing(job.getId());
        LocalDateTime now = LocalDateTime.now();

        // when
        executionService.executePendingBatch(10);
        assertThat(executionService.quarantineInterruptedBatch(now, 1)).isEqualTo(1);

        // then
        var partial = queryService.getJob(3L, job.getId());
        assertThat(partial.status()).isEqualTo(MailDispatchJobStatus.PROCESSING);
        assertThat(partial.unknownCount()).isEqualTo(1);
        assertThat(partial.processingCount()).isEqualTo(1);
        assertThat(executionService.quarantineInterruptedBatch(now, 1)).isEqualTo(1);
        assertThat(executionService.quarantineInterruptedBatch(now, 1)).isZero();
        executionService.executePendingBatch(10);
        var result = queryService.getJob(3L, job.getId());
        assertThat(result.status()).isEqualTo(MailDispatchJobStatus.UNKNOWN);
        assertThat(result.unknownCount()).isEqualTo(2);
        assertThat(result.processingCount()).isZero();
        assertThat(queryService.searchTargets(3L, job.getId(), MailDispatchTargetStatus.UNKNOWN,
                PageRequest.of(0, 10)).getContent()).hasSize(2).allSatisfy(target -> {
                    assertThat(target.failureReason()).isEqualTo("MAIL-21");
                    assertThat(target.sentAt()).isNull();
                });
        verifyNoInteractions(emailSendService);
    }

    @Test
    void 동시_복구_배치가_같은_만료_대상을_한_번만_격리하고_집계한다() throws Exception {
        // given
        var job = persistenceService.createJob(plan(10L), "fixture");
        var outbox = outboxRepository.findByDispatchJobIdAndApplyId(job.getId(), 10L).orElseThrow();
        LocalDateTime now = LocalDateTime.now().withNano(0);
        claimService.claim(outbox.getId(), now.minusMinutes(2), Duration.ofMinutes(2)).orElseThrow();
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try (var executor = Executors.newFixedThreadPool(2)) {
            Callable<Integer> recover = () -> {
                ready.countDown();
                if (!start.await(10, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("recovery start timed out");
                }
                return executionService.quarantineInterruptedBatch(now, 1);
            };
            var first = executor.submit(recover);
            var second = executor.submit(recover);
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();

            // when
            start.countDown();
            int quarantined = first.get(10, TimeUnit.SECONDS) + second.get(10, TimeUnit.SECONDS);

            // then
            assertThat(quarantined).isEqualTo(1);
            var result = queryService.getJob(3L, job.getId());
            assertThat(result.status()).isEqualTo(MailDispatchJobStatus.UNKNOWN);
            assertThat(result.unknownCount()).isEqualTo(1);
            assertThat(result.processingCount()).isZero();
            verifyNoInteractions(emailSendService);
        } finally {
            start.countDown();
        }
    }

    private MailDispatchPlan plan(Long... applyIds) {
        return new MailDispatchPlan(1L, 2L, 3L, UUID.randomUUID().toString(), "제목", "본문", Map.of(),
                List.of(applyIds).stream().map(applyId -> new MailDispatchPlan.Target(
                        applyId, applyId + "@ject.kr", "저장된 제목", "저장된 본문")).toList());
    }

    @TestConfiguration
    static class TestDependencies {

        @Bean
        Map2JsonSerializer map2JsonSerializer() {
            return new Map2JsonSerializer(new ObjectMapper());
        }
    }
}
