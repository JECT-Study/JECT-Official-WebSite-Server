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
import org.ject.support.admin.mail.exception.MailException;
import org.ject.support.admin.mail.repository.MailDispatchJobRepository;
import org.ject.support.admin.mail.repository.MailDispatchOutboxRepository;
import org.ject.support.base.TestSupport;
import org.ject.support.testconfig.QueryDslTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({MailDispatchClaimService.class, QueryDslTestConfig.class})
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
}
