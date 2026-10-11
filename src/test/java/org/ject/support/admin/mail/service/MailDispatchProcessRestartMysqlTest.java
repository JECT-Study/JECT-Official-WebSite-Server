package org.ject.support.admin.mail.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import org.ject.support.admin.mail.domain.MailDispatchJobStatus;
import org.ject.support.admin.mail.domain.MailDispatchTargetStatus;
import org.ject.support.admin.mail.repository.MailDispatchJobRepository;
import org.ject.support.admin.mail.repository.MailDispatchOutboxRepository;
import org.ject.support.admin.mail.repository.MailDispatchTargetRepository;
import org.ject.support.base.TestSupport;
import org.ject.support.common.response.ObjectMapperConfig;
import org.ject.support.common.util.Map2JsonSerializer;
import org.ject.support.testconfig.QueryDslTestConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({MailDispatchPersistenceService.class, MailDispatchQueryService.class,
        QueryDslTestConfig.class, Map2JsonSerializer.class, ObjectMapperConfig.class})
@Testcontainers
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class MailDispatchProcessRestartMysqlTest extends TestSupport {

    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.2");

    @Autowired
    private MailDispatchPersistenceService persistenceService;

    @Autowired
    private MailDispatchQueryService queryService;

    @Autowired
    private MailDispatchJobRepository jobRepository;

    @Autowired
    private MailDispatchOutboxRepository outboxRepository;

    @Autowired
    private MailDispatchTargetRepository targetRepository;

    @TempDir(cleanup = CleanupMode.ON_SUCCESS)
    private Path processLogs;

    private final List<Process> processes = new ArrayList<>();
    private final List<String> acceptedMails = new CopyOnWriteArrayList<>();
    private HttpServer mailServer;

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.driver-class-name", MYSQL::getDriverClassName);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.MySQLDialect");
    }

    @BeforeEach
    void 프로세스_검증_환경_준비() throws Exception {
        outboxRepository.deleteAllInBatch();
        targetRepository.deleteAllInBatch();
        jobRepository.deleteAllInBatch();
        mailServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        mailServer.createContext("/send", exchange -> {
            acceptedMails.add(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
        });
        mailServer.start();
    }

    @AfterEach
    void 검증용_프로세스와_메일_서버_종료() throws Exception {
        try {
            for (Process process : processes) {
                if (process.isAlive()) {
                    process.destroyForcibly();
                }
            }
            for (Process process : processes) {
                assertThat(process.waitFor(10, TimeUnit.SECONDS)).isTrue();
            }
        } finally {
            if (mailServer != null) {
                mailServer.stop(0);
            }
        }
    }

    @Test
    void 저장_직후_프로세스가_중단돼도_새_프로세스가_미처리_메일을_한_번만_발송한다() throws Exception {
        // given
        String key = UUID.randomUUID().toString();
        Process interrupted = startProcess("save-and-halt", key);
        assertThat(interrupted.waitFor(45, TimeUnit.SECONDS)).isTrue();
        assertThat(interrupted.exitValue()).isEqualTo(73);
        var savedJob = persistenceService.findJobByIdempotencyKey(3L, key).orElseThrow();
        assertThat(queryService.getJob(3L, savedJob.getId()).status()).isEqualTo(MailDispatchJobStatus.REQUESTED);
        assertThat(acceptedMails).isEmpty();

        // when
        Process recovered = startProcess("recover", key);
        assertThat(recovered.pid()).isNotEqualTo(interrupted.pid()).isNotEqualTo(ProcessHandle.current().pid());
        await().atMost(Duration.ofSeconds(45)).untilAsserted(() -> {
            assertThat(recovered.isAlive()).isTrue();
            assertThat(queryService.getJob(3L, savedJob.getId()).status()).isEqualTo(MailDispatchJobStatus.COMPLETED);
        });

        // then
        assertThat(queryService.getJob(3L, savedJob.getId()).successCount()).isEqualTo(1);
        assertThat(queryService.searchTargets(3L, savedJob.getId(), MailDispatchTargetStatus.SENT,
                PageRequest.of(0, 10)).getContent()).singleElement().satisfies(target -> {
                    assertThat(target.applyId()).isEqualTo(10L);
                    assertThat(target.attemptCount()).isEqualTo(1);
                });
        assertThat(acceptedMails).singleElement().satisfies(body -> {
            var payload = new ObjectMapper().readTree(body);
            assertThat(payload.get("to").asText()).isEqualTo("recipient@example.com");
            assertThat(payload.get("subject").asText()).isEqualTo("저장된 제목");
            assertThat(payload.get("body").asText()).isEqualTo("저장된 본문");
        });
        await().during(Duration.ofSeconds(1)).atMost(Duration.ofSeconds(3))
                .untilAsserted(() -> assertThat(acceptedMails).hasSize(1));
    }

    @Test
    void 외부_수락_직후_프로세스가_중단되면_새_프로세스는_재발송_없이_만료_후_격리한다() throws Exception {
        // given
        String key = UUID.randomUUID().toString();
        Process saved = startProcess("save-and-halt", key);
        assertThat(saved.waitFor(45, TimeUnit.SECONDS)).isTrue();
        assertThat(saved.exitValue()).isEqualTo(73);
        var job = persistenceService.findJobByIdempotencyKey(3L, key).orElseThrow();
        Process interrupted = startProcess("halt-after-accept", key);
        assertThat(interrupted.waitFor(45, TimeUnit.SECONDS)).isTrue();
        assertThat(interrupted.exitValue()).isEqualTo(74);
        assertThat(acceptedMails).hasSize(1);
        var interruptedResult = queryService.getJob(3L, job.getId());
        assertThat(interruptedResult.status()).isEqualTo(MailDispatchJobStatus.PROCESSING);
        assertThat(interruptedResult.successCount()).isZero();

        // when
        Process recovered = startProcess("recover", key);
        assertThat(recovered.pid()).isNotEqualTo(interrupted.pid()).isNotEqualTo(saved.pid());
        // 운영 lease 2분의 실제 만료를 기다리며 저장된 시각이나 정책은 수정하지 않고 검증
        await().atMost(Duration.ofMinutes(3)).pollInterval(Duration.ofSeconds(1)).untilAsserted(() -> {
            assertThat(recovered.isAlive()).isTrue();
            assertThat(acceptedMails).hasSize(1);
            assertThat(queryService.getJob(3L, job.getId()).status()).isEqualTo(MailDispatchJobStatus.UNKNOWN);
        });

        // then
        var result = queryService.getJob(3L, job.getId());
        assertThat(result.unknownCount()).isEqualTo(1);
        assertThat(result.successCount()).isZero();
        assertThat(result.failedCount()).isZero();
        assertThat(result.processingCount()).isZero();
        assertThat(queryService.searchTargets(3L, job.getId(), MailDispatchTargetStatus.UNKNOWN,
                PageRequest.of(0, 10)).getContent()).singleElement().satisfies(target -> {
                    assertThat(target.attemptCount()).isEqualTo(1);
                    assertThat(target.failureReason()).isEqualTo("MAIL-21");
                    assertThat(target.lastAttemptFailureReason()).isEqualTo("MAIL-21");
                    assertThat(target.sentAt()).isNull();
                    assertThat(target.nextAttemptAt()).isNull();
                });
        await().during(Duration.ofSeconds(1)).atMost(Duration.ofSeconds(3))
                .untilAsserted(() -> assertThat(acceptedMails).hasSize(1));
    }

    private Process startProcess(String mode, String key) throws Exception {
        ProcessBuilder builder = new ProcessBuilder(
                Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-Xmx256m", "-cp", System.getProperty("mail.dispatch.test.classpath"),
                "org.ject.support.testconfig.MailDispatchProcessTestApplication", mode, key);
        // 부모의 운영 설정·자격증명 상속을 차단하고 검증 DB 접속값만 환경으로 전달
        builder.environment().clear();
        builder.environment().put("MAIL_TEST_JDBC_URL", MYSQL.getJdbcUrl());
        builder.environment().put("MAIL_TEST_DB_USER", MYSQL.getUsername());
        builder.environment().put("MAIL_TEST_DB_PASSWORD", MYSQL.getPassword());
        builder.environment().put("MAIL_TEST_ENDPOINT", "http://127.0.0.1:" + mailServer.getAddress().getPort() + "/send");
        builder.redirectErrorStream(true).redirectOutput(processLogs.resolve(mode + "-" + processes.size() + ".log").toFile());
        Process process = builder.start();
        processes.add(process);
        return process;
    }
}
