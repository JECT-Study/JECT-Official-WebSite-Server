package org.ject.support.admin.mail.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import org.ject.support.admin.mail.domain.MailDispatchJob;
import org.ject.support.admin.mail.domain.MailDispatchJobStatus;
import org.ject.support.admin.mail.domain.MailDispatchExclusionReason;
import org.ject.support.admin.mail.domain.MailDispatchOutboxStatus;
import org.ject.support.admin.mail.domain.MailDispatchTargetStatus;
import org.ject.support.admin.mail.domain.MailScenarioType;
import org.ject.support.admin.mail.dto.MailDispatchResponse;
import org.ject.support.admin.mail.dto.ScheduleMailDispatchRequest;
import org.ject.support.admin.mail.dto.SendMailDispatchRequest;
import org.ject.support.admin.mail.exception.MailErrorCode;
import org.ject.support.admin.mail.exception.MailException;
import org.ject.support.admin.mail.repository.MailDispatchJobRepository;
import org.ject.support.admin.mail.repository.MailDispatchOutboxRepository;
import org.ject.support.base.TestSupport;
import org.ject.support.common.util.Map2JsonSerializer;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.apply.domain.SelectionResult;
import org.ject.support.domain.apply.dto.ApplyEligibilityFacts;
import org.ject.support.domain.apply.service.ApplyQueryService;
import org.ject.support.external.email.exception.EmailErrorCode;
import org.ject.support.external.email.exception.EmailException;
import org.ject.support.external.email.service.EmailSendService;
import org.ject.support.testconfig.QueryDslTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        MailDispatchPersistenceService.class,
        MailDispatchUseCase.class,
        MailDispatchExecutionService.class,
        MailDispatchClaimService.class,
        MailDispatchScheduledActivationService.class,
        MailDispatchCancellationService.class,
        MailDispatchQueryService.class,
        QueryDslTestConfig.class,
        MailDispatchUseCaseMysqlIntegrationTest.TestDependencies.class
})
@Testcontainers
class MailDispatchUseCaseMysqlIntegrationTest extends TestSupport {

    private static final AtomicLong NEXT_SCHEDULED_APPLY_ID = new AtomicLong(900_000L);

    @Container
    private static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.2");

    @Autowired
    private MailDispatchUseCase mailDispatchUseCase;

    @MockitoSpyBean
    private MailDispatchJobRepository mailDispatchJobRepository;

    @Autowired
    private MailDispatchOutboxRepository outboxRepository;

    @Autowired
    private MailDispatchQueryService queryService;

    @Autowired
    private MailDispatchExecutionService executionService;

    @Autowired
    private MailDispatchPersistenceService persistenceService;

    @Autowired
    private MailDispatchClaimService claimService;

    @Autowired
    private MailDispatchScheduledActivationService scheduledActivationService;

    @Autowired
    private MailDispatchCancellationService cancellationService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private MailDispatchPreparationService preparationService;

    @MockitoBean
    private EmailSendService emailSendService;

    @MockitoBean
    private ApplyQueryService applyQueryService;

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.driver-class-name", mysqlContainer::getDriverClassName);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.MySQLDialect");
    }

    @Test
    @DisplayName("모든 수신자 실패를 최종 결과로 저장하고 같은 요청의 재발송을 막는다")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void 모든_수신자_실패를_최종_결과로_저장하고_같은_요청의_재발송을_막는다() {
        // given
        Long requestedByAdminId = 5L;
        String idempotencyKey = "all-failed-dispatch-key";
        SendMailDispatchRequest request = new SendMailDispatchRequest(
                2L, 1L, List.of(10L, 20L), "안내 제목", Map.of());
        MailDispatchPlan plan = new MailDispatchPlan(
                1L,
                2L,
                requestedByAdminId,
                idempotencyKey,
                "안내 제목",
                "안내 본문",
                Map.of(),
                List.of(
                        new MailDispatchPlan.Target(10L, "first@ject.kr", "안내 제목", "안내 본문"),
                        new MailDispatchPlan.Target(20L, "second@ject.kr", "안내 제목", "안내 본문"))
        );
        given(preparationService.prepare(request, requestedByAdminId, idempotencyKey))
                .willReturn(plan);
        willThrow(new EmailException(EmailErrorCode.EMAIL_SEND_FAILURE))
                .given(emailSendService).sendEmail(anyString(), anyString(), anyString());

        // when
        MailDispatchResponse first = mailDispatchUseCase.sendMail(request, requestedByAdminId, idempotencyKey);
        MailDispatchResponse repeated = mailDispatchUseCase.sendMail(request, requestedByAdminId, idempotencyKey);

        // then
        assertThat(first.status()).isEqualTo(MailDispatchJobStatus.FAILED);
        assertThat(first.targetCount()).isEqualTo(2);
        assertThat(first.processingCount()).isZero();
        assertThat(first.successCount()).isZero();
        assertThat(first.failedCount()).isEqualTo(2);
        assertThat(repeated).isEqualTo(first);
        verify(emailSendService, times(2)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("큰 입력 변수와 최대 500명 대상 요청을 저장하고 같은 요청을 재사용한다")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void 큰_입력_변수와_최대_500명_대상_요청을_저장하고_같은_요청을_재사용한다() {
        // given
        Long requestedByAdminId = 4L;
        String idempotencyKey = "large-dispatch-key";
        String message = "x".repeat(60_000);
        List<Long> applyIds = LongStream.range(1_000_000_000L, 1_000_000_500L)
                .boxed()
                .toList();
        Map<String, String> inputVariables = Map.of("MESSAGE", message);
        SendMailDispatchRequest request = new SendMailDispatchRequest(
                2L, 1L, applyIds, "안내 제목", inputVariables);
        MailDispatchPlan plan = new MailDispatchPlan(
                1L,
                2L,
                requestedByAdminId,
                idempotencyKey,
                "안내 제목",
                "{{MESSAGE}}",
                inputVariables,
                applyIds.stream()
                        .map(applyId -> new MailDispatchPlan.Target(
                                applyId, "applicant@ject.kr", "안내 제목", message))
                        .toList()
        );
        given(preparationService.prepare(request, requestedByAdminId, idempotencyKey))
                .willReturn(plan);

        // when
        MailDispatchResponse first = mailDispatchUseCase.sendMail(request, requestedByAdminId, idempotencyKey);
        MailDispatchResponse repeated = mailDispatchUseCase.sendMail(request, requestedByAdminId, idempotencyKey);

        // then
        assertThat(first.targetCount()).isEqualTo(500);
        assertThat(first.successCount()).isEqualTo(500);
        assertThat(first.failedCount()).isZero();
        assertThat(repeated).isEqualTo(first);
        verify(emailSendService, times(500)).sendEmail("applicant@ject.kr", "안내 제목", message);
    }

    @Test
    @DisplayName("선행 조회 뒤 발생한 MySQL 키 충돌은 요청 본문 불일치로 반환한다")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void 선행_조회_뒤_발생한_MySQL_키_충돌은_요청_본문_불일치로_반환한다() {
        // given
        Long requestedByAdminId = 3L;
        String idempotencyKey = "dispatch-key";
        SendMailDispatchRequest request = new SendMailDispatchRequest(
                2L, 1L, List.of(10L), "새 제목", Map.of());
        MailDispatchPlan plan = new MailDispatchPlan(
                1L,
                2L,
                requestedByAdminId,
                idempotencyKey,
                "새 제목",
                "새 본문",
                Map.of(),
                List.of(new MailDispatchPlan.Target(10L, "applicant@ject.kr", "새 제목", "새 본문"))
        );
        given(preparationService.prepare(request, requestedByAdminId, idempotencyKey))
                .willAnswer(invocation -> {
                    mailDispatchJobRepository.saveAndFlush(MailDispatchJob.create(
                            1L,
                            2L,
                            requestedByAdminId,
                            idempotencyKey,
                            "기존 제목",
                            "기존 본문",
                            "{}",
                            "existing-fingerprint",
                            1
                    ));
                    return plan;
                });

        // when & then
        assertThatThrownBy(() -> mailDispatchUseCase.sendMail(request, requestedByAdminId, idempotencyKey))
                .isInstanceOf(MailException.class)
                .extracting("errorCode")
                .isEqualTo(MailErrorCode.IDEMPOTENCY_KEY_PAYLOAD_MISMATCH);
        assertThat(mailDispatchJobRepository
                .findByRequestedByAdminIdAndIdempotencyKey(requestedByAdminId, idempotencyKey))
                .get()
                .extracting(MailDispatchJob::getRequestFingerprint)
                .isEqualTo("existing-fingerprint");
        verifyNoInteractions(emailSendService);
    }

    @ParameterizedTest
    @MethodSource("uncertainEmailFailures")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void 불확실한_결과는_실패와_구분해_조회하고_같은_요청에서_재발송하지_않는다(RuntimeException exception) {
        // given
        SendMailDispatchRequest request = new SendMailDispatchRequest(
                2L, 1L, List.of(31L, 32L), "제목", Map.of());
        String key = "uncertain-dispatch-key-" + exception.getClass().getSimpleName();
        MailDispatchPlan plan = new MailDispatchPlan(1L, 2L, 3L, key, "제목", "본문", Map.of(),
                List.of(new MailDispatchPlan.Target(31L, "uncertain@ject.kr", "제목", "본문"),
                        new MailDispatchPlan.Target(32L, "success@ject.kr", "제목", "본문")));
        given(preparationService.prepare(request, 3L, key)).willReturn(plan);
        doThrow(exception)
                .when(emailSendService).sendEmail("uncertain@ject.kr", "제목", "본문");

        // when
        var result = mailDispatchUseCase.sendMail(request, 3L, key);
        var repeated = mailDispatchUseCase.sendMail(request, 3L, key);

        // then
        assertThat(result.status()).isEqualTo(MailDispatchJobStatus.UNKNOWN);
        assertThat(result.unknownCount()).isEqualTo(1);
        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.failedCount()).isZero();
        assertThat(result.processingCount()).isZero();
        assertThat(repeated).isEqualTo(result);
        var detail = queryService.getJob(3L, result.dispatchJobId());
        assertThat(detail.unknownCount()).isEqualTo(1);
        assertThat(queryService.searchTargets(3L, result.dispatchJobId(), MailDispatchTargetStatus.UNKNOWN,
                PageRequest.of(0, 10)).getContent()).singleElement().satisfies(target -> {
                    assertThat(target.applyId()).isEqualTo(31L);
                    assertThat(target.failureReason()).isEqualTo("MAIL-21");
                    assertThat(target.sentAt()).isNull();
                });
        assertThat(outboxRepository.findByDispatchJobIdAndApplyId(result.dispatchJobId(), 31L))
                .get().satisfies(outbox -> {
                    assertThat(outbox.getStatus()).isEqualTo(MailDispatchOutboxStatus.UNKNOWN);
                    assertThat(outbox.getFailureReason()).isEqualTo("MAIL-21");
                });
        verify(emailSendService, times(1)).sendEmail("uncertain@ject.kr", "제목", "본문");
        verify(emailSendService, times(1)).sendEmail("success@ject.kr", "제목", "본문");
    }

    private static Stream<RuntimeException> uncertainEmailFailures() {
        return Stream.of(new EmailException(EmailErrorCode.EMAIL_SEND_RESULT_UNKNOWN),
                new RuntimeException("unknown provider outcome"));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void 즉시_발송도_claim을_커밋한_뒤_DB_트랜잭션_없이_SES를_호출한다() {
        // given
        String key = "claim-before-send";
        SendMailDispatchRequest request = new SendMailDispatchRequest(2L, 1L, List.of(41L), "제목", Map.of());
        MailDispatchPlan plan = new MailDispatchPlan(1L, 2L, 3L, key, "제목", "본문", Map.of(),
                List.of(new MailDispatchPlan.Target(41L, "claimed@ject.kr", "제목", "본문")));
        given(preparationService.prepare(request, 3L, key)).willReturn(plan);
        doAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
            var job = mailDispatchJobRepository.findByRequestedByAdminIdAndIdempotencyKey(3L, key).orElseThrow();
            var outbox = outboxRepository.findByDispatchJobIdAndApplyId(job.getId(), 41L).orElseThrow();
            assertThat(outbox.getStatus()).isEqualTo(MailDispatchOutboxStatus.PROCESSING);
            assertThat(outbox.getClaimToken()).isNotBlank();
            assertThat(job.getClaimStartedAt()).isNotNull();
            return null;
        }).when(emailSendService).sendEmail("claimed@ject.kr", "제목", "본문");

        // when
        var result = mailDispatchUseCase.sendMail(request, 3L, key);

        // then
        assertThat(result.status()).isEqualTo(MailDispatchJobStatus.COMPLETED);
        assertThat(result.successCount()).isEqualTo(1);
    }

    @ParameterizedTest
    @EnumSource(value = EmailErrorCode.class, names = {"EMAIL_SEND_FAILURE", "TOO_MANY_EMAIL_REQUESTS"})
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void 확정_실패를_기록하고_나머지_대상은_계속_처리한다(EmailErrorCode errorCode) {
        // given
        String key = "confirmed-failure-" + errorCode.name();
        SendMailDispatchRequest request = new SendMailDispatchRequest(2L, 1L, List.of(71L, 72L), "제목", Map.of());
        MailDispatchPlan plan = new MailDispatchPlan(1L, 2L, 3L, key, "제목", "본문", Map.of(),
                List.of(new MailDispatchPlan.Target(71L, "failed@ject.kr", "제목", "본문"),
                        new MailDispatchPlan.Target(72L, "remaining@ject.kr", "제목", "본문")));
        given(preparationService.prepare(request, 3L, key)).willReturn(plan);
        doThrow(new EmailException(errorCode)).when(emailSendService).sendEmail("failed@ject.kr", "제목", "본문");

        // when
        var result = mailDispatchUseCase.sendMail(request, 3L, key);

        // then
        assertThat(result.status()).isEqualTo(MailDispatchJobStatus.COMPLETED);
        assertThat(result.failedCount()).isEqualTo(1);
        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.unknownCount()).isZero();
        assertThat(result.processingCount()).isZero();
        assertThat(queryService.searchTargets(3L, result.dispatchJobId(), MailDispatchTargetStatus.FAILED,
                PageRequest.of(0, 10)).getContent()).singleElement()
                .satisfies(target -> assertThat(target.failureReason()).isEqualTo(errorCode.getCode()));
        verify(emailSendService).sendEmail("remaining@ject.kr", "제목", "본문");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void SES_성공_후_DB_기록에_실패하면_재발송하지_않고_만료_후_격리한다() {
        // given
        String key = "result-record-failure";
        SendMailDispatchRequest request = new SendMailDispatchRequest(2L, 1L, List.of(51L), "제목", Map.of());
        MailDispatchPlan plan = new MailDispatchPlan(1L, 2L, 3L, key, "제목", "본문", Map.of(),
                List.of(new MailDispatchPlan.Target(51L, "accepted@ject.kr", "제목", "본문")));
        given(preparationService.prepare(request, 3L, key)).willReturn(plan);
        jdbcTemplate.execute("ALTER TABLE mail_dispatch_outbox ADD CONSTRAINT simulated_execution_result_failure "
                + "CHECK (apply_id <> 51 OR status <> 'SENT')");

        try {
            // when
            assertThatThrownBy(() -> mailDispatchUseCase.sendMail(request, 3L, key))
                    .hasRootCauseMessage("Check constraint 'simulated_execution_result_failure' is violated.");
            var job = mailDispatchJobRepository.findByRequestedByAdminIdAndIdempotencyKey(3L, key).orElseThrow();
            var outbox = outboxRepository.findByDispatchJobIdAndApplyId(job.getId(), 51L).orElseThrow();

            // then
            var pendingResult = queryService.getJob(3L, job.getId());
            assertThat(pendingResult.processingCount()).isEqualTo(1);
            assertThat(pendingResult.successCount()).isZero();
            assertThat(outbox.getStatus()).isEqualTo(MailDispatchOutboxStatus.PROCESSING);
            executionService.execute(outbox.getId());
            assertThat(claimService.quarantineExpired(outbox.getId(), LocalDateTime.now().plusMinutes(3)))
                    .isTrue();
            executionService.execute(outbox.getId());
            var result = mailDispatchUseCase.sendMail(request, 3L, key);
            assertThat(result.status()).isEqualTo(MailDispatchJobStatus.UNKNOWN);
            assertThat(result.unknownCount()).isEqualTo(1);
            assertThat(result.successCount()).isZero();
            assertThat(result.failedCount()).isZero();
            assertThat(result.processingCount()).isZero();
            verify(emailSendService, times(1)).sendEmail("accepted@ject.kr", "제목", "본문");
        } finally {
            jdbcTemplate.execute("ALTER TABLE mail_dispatch_outbox DROP CHECK simulated_execution_result_failure");
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void 두_실행자가_같은_대상을_처리해도_저장된_snapshot으로_한_번만_발송한다() throws Exception {
        // given
        MailDispatchPlan plan = new MailDispatchPlan(1L, 2L, 3L, "concurrent-execution", "원본 제목", "원본 본문",
                Map.of(), List.of(new MailDispatchPlan.Target(61L, "snapshot@ject.kr", "저장된 제목", "저장된 본문")));
        var job = persistenceService.createJob(plan, "fixture");
        var outbox = outboxRepository.findByDispatchJobIdAndApplyId(job.getId(), 61L).orElseThrow();
        CountDownLatch sendStarted = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        doAnswer(invocation -> {
            sendStarted.countDown();
            assertThat(release.await(10, TimeUnit.SECONDS)).isTrue();
            return null;
        }).when(emailSendService).sendEmail("snapshot@ject.kr", "저장된 제목", "저장된 본문");

        try (var executor = Executors.newFixedThreadPool(2)) {
            // when
            var first = executor.submit(() -> executionService.execute(outbox.getId()));
            assertThat(sendStarted.await(10, TimeUnit.SECONDS)).isTrue();
            var second = executor.submit(() -> executionService.execute(outbox.getId()));
            second.get(10, TimeUnit.SECONDS);
            release.countDown();
            first.get(10, TimeUnit.SECONDS);
            executionService.execute(outbox.getId());

            // then
            var result = queryService.getJob(3L, job.getId());
            assertThat(result.status()).isEqualTo(MailDispatchJobStatus.COMPLETED);
            assertThat(result.successCount()).isEqualTo(1);
            assertThat(result.processingCount()).isZero();
            verify(emailSendService, times(1)).sendEmail("snapshot@ject.kr", "저장된 제목", "저장된 본문");
        } finally {
            release.countDown();
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void worker_활성화_요청은_발송_의도를_저장하고_외부_호출을_기다리지_않는다() {
        // given
        String key = "async-submission";
        var request = new SendMailDispatchRequest(2L, 1L, List.of(81L), "제목", Map.of());
        var plan = new MailDispatchPlan(1L, 2L, 3L, key, "제목", "본문", Map.of(),
                List.of(new MailDispatchPlan.Target(81L, "queued@ject.kr", "저장된 제목", "저장된 본문")));
        given(preparationService.prepare(request, 3L, key)).willReturn(plan);

        // when
        new ApplicationContextRunner()
                .withUserConfiguration(TestDependencies.class)
                .withBean(MailDispatchUseCase.class)
                .withBean(MailDispatchPreparationService.class, () -> preparationService)
                .withBean(MailDispatchPersistenceService.class, () -> persistenceService)
                .withBean(MailDispatchExecutionService.class, () -> executionService)
                .withPropertyValues("mail.dispatch.worker.enabled=true")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    var result = context.getBean(MailDispatchUseCase.class).sendMail(request, 3L, key);

                    // then
                    assertThat(result.status()).isEqualTo(MailDispatchJobStatus.REQUESTED);
                    assertThat(result.successCount()).isZero();
                    verifyNoInteractions(emailSendService);
                    executionService.executePendingBatch(1);
                    assertThat(queryService.getJob(3L, result.dispatchJobId()).status())
                            .isEqualTo(MailDispatchJobStatus.COMPLETED);
                    verify(emailSendService).sendEmail("queued@ject.kr", "저장된 제목", "저장된 본문");
                });
    }

    @Test
    @DisplayName("예약 시각 전에는 대기하고 도래 뒤에는 저장된 본문으로 한 번 발송한다")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void 예약시각_전에는_대기하고_도래_후_저장된_본문으로_한번_발송한다() {
        // given
        long applyId = NEXT_SCHEDULED_APPLY_ID.incrementAndGet();
        MailDispatchJob job = createScheduledJob(MailScenarioType.ETC, List.of(applyId));
        stubEligibility(Map.of(applyId, eligibility(applyId, 2L, ApplyStatus.SUBMITTED,
                SelectionResult.FAILED)));
        MailDispatchWorkerService worker = new MailDispatchWorkerService(executionService,
                scheduledActivationService, 10);

        // when
        worker.poll();

        // then
        assertThat(queryService.getJob(3L, job.getId()).status()).isEqualTo(MailDispatchJobStatus.SCHEDULED);
        verifyNoInteractions(emailSendService);

        // 예약 상태는 실제 API로 만들고, 시각만 지나게 해 복구 후 지연 실행을 검증한다.
        makeDue(job);
        worker.poll();

        assertThat(queryService.getJob(3L, job.getId())).satisfies(result -> {
            assertThat(result.status()).isEqualTo(MailDispatchJobStatus.COMPLETED);
            assertThat(result.targetCount()).isEqualTo(1);
            assertThat(result.successCount()).isEqualTo(1);
            assertThat(result.excludedCount()).isZero();
            assertThat(result.processingCount()).isZero();
        });
        verify(emailSendService, times(1)).sendEmail("scheduled-" + applyId + "@ject.kr",
                "저장된 제목 " + applyId, "저장된 본문 " + applyId);
    }

    @Test
    @DisplayName("일반 안내는 선정 변경을 유지하고 결과 안내는 변경된 대상을 제외한다")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void 일반안내_선정변경은_유지하고_결과안내_선정변경은_제외한다() {
        // given
        long etcApplyId = NEXT_SCHEDULED_APPLY_ID.incrementAndGet();
        long resultApplyId = NEXT_SCHEDULED_APPLY_ID.incrementAndGet();
        MailDispatchJob etcJob = createScheduledJob(MailScenarioType.ETC, List.of(etcApplyId));
        MailDispatchJob resultJob = createScheduledJob(MailScenarioType.FIRST_PASS, List.of(resultApplyId));
        stubEligibility(Map.of(
                etcApplyId, eligibility(etcApplyId, 2L, ApplyStatus.SUBMITTED, SelectionResult.FAILED),
                resultApplyId, eligibility(resultApplyId, 2L, ApplyStatus.SUBMITTED, SelectionResult.FAILED)));
        makeDue(etcJob);
        makeDue(resultJob);
        MailDispatchWorkerService worker = new MailDispatchWorkerService(executionService,
                scheduledActivationService, 10);

        // when
        worker.poll();

        // then
        assertThat(queryService.getJob(3L, etcJob.getId()).successCount()).isEqualTo(1);
        assertThat(queryService.getJob(3L, etcJob.getId()).excludedCount()).isZero();
        assertThat(queryService.getJob(3L, resultJob.getId())).satisfies(result -> {
            assertThat(result.status()).isEqualTo(MailDispatchJobStatus.COMPLETED);
            assertThat(result.targetCount()).isEqualTo(1);
            assertThat(result.successCount()).isZero();
            assertThat(result.excludedCount()).isEqualTo(1);
            assertThat(result.failedCount()).isZero();
            assertThat(result.processingCount()).isZero();
        });
        assertThat(queryService.searchTargets(3L, resultJob.getId(), MailDispatchTargetStatus.EXCLUDED,
                PageRequest.of(0, 10)).getContent()).singleElement().satisfies(target -> {
                    assertThat(target.applyId()).isEqualTo(resultApplyId);
                    assertThat(target.exclusionReason())
                            .isEqualTo(MailDispatchExclusionReason.SELECTION_RESULT_CHANGED.name());
                    assertThat(target.failureReason()).isNull();
                });
        verify(emailSendService, times(1)).sendEmail("scheduled-" + etcApplyId + "@ject.kr",
                "저장된 제목 " + etcApplyId, "저장된 본문 " + etcApplyId);
    }

    @Test
    @DisplayName("삭제·반려·모집 불일치는 개인정보 없는 사유로 제외하고 전원 제외를 완료한다")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void 삭제_반려_모집불일치는_사유와_함께_제외하고_전원제외를_완료한다() {
        // given
        long missingApplyId = NEXT_SCHEDULED_APPLY_ID.incrementAndGet();
        long rejectedApplyId = NEXT_SCHEDULED_APPLY_ID.incrementAndGet();
        long otherRecruitApplyId = NEXT_SCHEDULED_APPLY_ID.incrementAndGet();
        MailDispatchJob job = createScheduledJob(MailScenarioType.FINAL_PASS,
                List.of(missingApplyId, rejectedApplyId, otherRecruitApplyId));
        stubEligibility(Map.of(
                rejectedApplyId, eligibility(rejectedApplyId, 2L, ApplyStatus.REJECTED, SelectionResult.PASSED),
                otherRecruitApplyId, eligibility(otherRecruitApplyId, 99L, ApplyStatus.SUBMITTED,
                        SelectionResult.PASSED)));
        makeDue(job);

        // when
        new MailDispatchWorkerService(executionService, scheduledActivationService, 10).poll();

        // then
        assertThat(queryService.getJob(3L, job.getId())).satisfies(result -> {
            assertThat(result.status()).isEqualTo(MailDispatchJobStatus.COMPLETED);
            assertThat(result.targetCount()).isEqualTo(3);
            assertThat(result.processingCount()).isZero();
            assertThat(result.excludedCount()).isEqualTo(3);
            assertThat(result.failedCount()).isZero();
            assertThat(result.successCount()).isZero();
            assertThat(result.finishedAt()).isNotNull();
        });
        var excludedTargets = queryService.searchTargets(3L, job.getId(), MailDispatchTargetStatus.EXCLUDED,
                PageRequest.of(0, 10)).getContent();
        assertThat(excludedTargets).hasSize(3);
        assertThat(excludedTargets).extracting(target -> target.exclusionReason())
                .containsExactlyInAnyOrder(
                        MailDispatchExclusionReason.APPLY_NOT_FOUND.name(),
                        MailDispatchExclusionReason.APPLY_NOT_SUBMITTED.name(),
                        MailDispatchExclusionReason.RECRUIT_MISMATCH.name());
        assertThat(excludedTargets).allSatisfy(target -> assertThat(target.failureReason()).isNull());
        assertThat(outboxRepository.findAllByDispatchJobIdOrderByIdAsc(job.getId()))
                .allSatisfy(outbox -> assertThat(outbox.getStatus()).isEqualTo(MailDispatchOutboxStatus.EXCLUDED));
        verifyNoInteractions(emailSendService);
    }

    @Test
    @DisplayName("재시도 claim은 자격을 다시 확인하고 동시 제외를 한 번만 집계한다")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void 재시도_claim은_자격을_재확인하고_동시제외를_한번만_집계한다() throws Exception {
        // given
        long applyId = NEXT_SCHEDULED_APPLY_ID.incrementAndGet();
        MailDispatchJob job = createScheduledJob(MailScenarioType.STANDBY_PASS, List.of(applyId));
        makeDue(job);
        assertThat(scheduledActivationService.activateDueJobs(Instant.now(), 10)).isEqualTo(1);
        Long outboxId = outboxRepository.findByDispatchJobIdAndApplyId(job.getId(), applyId)
                .orElseThrow().getId();
        LocalDateTime claimTime = LocalDateTime.now();
        var initialEligibility = eligibility(applyId, 2L, ApplyStatus.SUBMITTED, SelectionResult.PASSED);
        var changedEligibility = eligibility(applyId, 2L, ApplyStatus.SUBMITTED, SelectionResult.FAILED);
        given(applyQueryService.findCurrentEligibilityFactsByApplyIds(anyList()))
                .willReturn(List.of(initialEligibility), List.of(changedEligibility));
        var firstClaim = claimService.claim(outboxId, claimTime, Duration.ofMinutes(2)).orElseThrow();
        assertThat(claimService.recordThrottling(outboxId, firstClaim.claimToken(), claimTime.plusSeconds(1)))
                .isTrue();
        assertThat(claimService.claim(outboxId, claimTime.plusSeconds(30), Duration.ofMinutes(2))).isEmpty();

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        Callable<Boolean> retryClaim = () -> {
            ready.countDown();
            assertThat(start.await(10, TimeUnit.SECONDS)).isTrue();
            return claimService.claim(outboxId, claimTime.plusSeconds(31), Duration.ofMinutes(2)).isPresent();
        };

        // when
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(retryClaim);
            var second = executor.submit(retryClaim);
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            // then
            assertThat(first.get(10, TimeUnit.SECONDS)).isFalse();
            assertThat(second.get(10, TimeUnit.SECONDS)).isFalse();
        }
        verify(applyQueryService, times(2)).findCurrentEligibilityFactsByApplyIds(anyList());
        assertThat(queryService.getJob(3L, job.getId())).satisfies(result -> {
            assertThat(result.status()).isEqualTo(MailDispatchJobStatus.COMPLETED);
            assertThat(result.targetCount()).isEqualTo(1);
            assertThat(result.processingCount()).isZero();
            assertThat(result.excludedCount()).isEqualTo(1);
            assertThat(result.failedCount()).isZero();
        });
        assertThat(outboxRepository.findById(outboxId)).get().satisfies(outbox -> {
            assertThat(outbox.getStatus()).isEqualTo(MailDispatchOutboxStatus.EXCLUDED);
            assertThat(outbox.getAttemptCount()).isEqualTo(1);
        });
        verifyNoInteractions(emailSendService);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("판단 정보가 없는 과거 예약을 최신 템플릿으로 추정해 발송하지 않는다")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void 예약의_유형이나_선정결과가_없으면_사유를_남기고_제외한다(boolean missingType) {
        // given
        long applyId = NEXT_SCHEDULED_APPLY_ID.incrementAndGet();
        MailDispatchJob job = createScheduledJob(missingType ? null : MailScenarioType.FIRST_PASS,
                List.of(applyId));
        if (!missingType) {
            jdbcTemplate.update("UPDATE mail_dispatch_target SET selection_result_snapshot = NULL "
                    + "WHERE dispatch_job_id = ?", job.getId());
        }
        stubEligibility(Map.of(applyId, eligibility(applyId, 2L, ApplyStatus.SUBMITTED, SelectionResult.PASSED)));
        makeDue(job);

        // when
        new MailDispatchWorkerService(executionService, scheduledActivationService, 10).poll();

        // then
        assertThat(queryService.getJob(3L, job.getId())).satisfies(result -> {
            assertThat(result.status()).isEqualTo(MailDispatchJobStatus.COMPLETED);
            assertThat(result.excludedCount()).isEqualTo(1);
            assertThat(result.successCount()).isZero();
            assertThat(result.failedCount()).isZero();
        });
        assertThat(queryService.searchTargets(3L, job.getId(), MailDispatchTargetStatus.EXCLUDED,
                PageRequest.of(0, 10)).getContent()).singleElement().satisfies(target ->
                assertThat(target.exclusionReason()).isEqualTo((missingType
                        ? MailDispatchExclusionReason.SCENARIO_TYPE_SNAPSHOT_MISSING
                        : MailDispatchExclusionReason.SELECTION_RESULT_SNAPSHOT_MISSING).name()));
        verifyNoInteractions(emailSendService);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("취소와 활성화가 겹쳐도 작업 잠금을 먼저 획득한 처리만 적용한다")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void 취소와_활성화가_경합하면_잠금을_먼저_획득한_처리만_적용한다(boolean cancelFirst) throws Exception {
        // given
        MailDispatchJob job = createScheduledJob(MailScenarioType.ETC,
                List.of(NEXT_SCHEDULED_APPLY_ID.incrementAndGet()));
        makeDue(job);
        CountDownLatch firstLocked = new CountDownLatch(1);
        CountDownLatch contenderEntered = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        AtomicBoolean firstAcquirer = new AtomicBoolean(true);
        var repositoryAnswer = mockingDetails(mailDispatchJobRepository).getMockCreationSettings().getDefaultAnswer();
        // 실제 SELECT FOR UPDATE 뒤 첫 트랜잭션을 멈춰 반대 요청의 잠금 대기를 검증한다.
        doAnswer(invocation -> {
            boolean first = firstAcquirer.getAndSet(false);
            if (!first) {
                contenderEntered.countDown();
            }
            Object result = repositoryAnswer.answer(invocation);
            if (first) {
                firstLocked.countDown();
                assertThat(releaseFirst.await(10, TimeUnit.SECONDS)).isTrue();
            }
            return result;
        }).when(mailDispatchJobRepository).findByIdForUpdate(job.getId());
        Callable<Boolean> cancel = () -> {
            try {
                cancellationService.cancelMail(3L, job.getId());
                return true;
            } catch (MailException exception) {
                assertThat(exception.getErrorCode()).isEqualTo(MailErrorCode.DISPATCH_CANCELLATION_NOT_ALLOWED);
                return false;
            }
        };
        Callable<Boolean> activate = () -> scheduledActivationService.activateDueJobs(Instant.now(), 10) == 1;

        // when
        try (var executor = Executors.newFixedThreadPool(2)) {
            try {
                var first = executor.submit(cancelFirst ? cancel : activate);
                boolean locked = firstLocked.await(10, TimeUnit.SECONDS);
                if (!locked && first.isDone()) {
                    first.get(1, TimeUnit.SECONDS);
                }
                assertThat(locked).isTrue();
                var second = executor.submit(cancelFirst ? activate : cancel);
                assertThat(contenderEntered.await(10, TimeUnit.SECONDS)).isTrue();
                assertThatThrownBy(() -> second.get(200, TimeUnit.MILLISECONDS))
                        .isInstanceOf(TimeoutException.class);
                releaseFirst.countDown();

                // then
                assertThat(first.get(10, TimeUnit.SECONDS)).isTrue();
                assertThat(second.get(10, TimeUnit.SECONDS)).isFalse();
            } finally {
                releaseFirst.countDown();
            }
        }
        assertThat(queryService.getJob(3L, job.getId())).satisfies(result -> {
            assertThat(result.status()).isEqualTo(cancelFirst
                    ? MailDispatchJobStatus.CANCELLED : MailDispatchJobStatus.PROCESSING);
            assertThat(result.excludedCount()).isZero();
            assertThat(result.successCount()).isZero();
        });
        if (!cancelFirst) {
            assertThat(mailDispatchJobRepository.findById(job.getId()).orElseThrow().getClaimStartedAt())
                    .isNotNull();
        }
        verifyNoInteractions(emailSendService);
    }

    private MailDispatchJob createScheduledJob(MailScenarioType scenarioType, List<Long> applyIds) {
        String key = "scheduled-" + UUID.randomUUID();
        Instant scheduledAt = Instant.now().plus(Duration.ofMinutes(10))
                .truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
        ScheduleMailDispatchRequest request = new ScheduleMailDispatchRequest(
                2L, 1L, applyIds, "예약 제목", Map.of(), OffsetDateTime.ofInstant(scheduledAt, ZoneOffset.UTC));
        MailDispatchPlan plan = new MailDispatchPlan(1L, 2L, 3L, key, "원본 제목 템플릿", "원본 본문 템플릿",
                Map.of(), applyIds.stream().map(applyId -> new MailDispatchPlan.Target(
                        applyId, "scheduled-" + applyId + "@ject.kr", "저장된 제목 " + applyId,
                        "저장된 본문 " + applyId, SelectionResult.PASSED.name())).toList(), scenarioType);
        given(preparationService.prepare(new SendMailDispatchRequest(
                request.recruitId(), request.scenarioId(), request.applyIds(), request.subjectOverride(),
                request.inputVariables()), 3L, key)).willReturn(plan);
        mailDispatchUseCase.scheduleMail(request, 3L, key);
        return mailDispatchJobRepository.findByRequestedByAdminIdAndIdempotencyKey(3L, key).orElseThrow();
    }

    private void makeDue(MailDispatchJob job) {
        assertThat(jdbcTemplate.update("""
                UPDATE mail_dispatch_job
                SET scheduled_at = DATE_SUB(scheduled_at, INTERVAL 20 MINUTE)
                WHERE id = ?
                """, job.getId())).isOne();
    }

    private void stubEligibility(Map<Long, ApplyEligibilityFacts> factsByApplyId) {
        given(applyQueryService.findCurrentEligibilityFactsByApplyIds(anyList()))
                .willAnswer(invocation -> {
                    List<Long> requestedApplyIds = invocation.getArgument(0);
                    return requestedApplyIds.stream()
                            .map(factsByApplyId::get)
                            .filter(Objects::nonNull)
                            .toList();
                });
    }

    private ApplyEligibilityFacts eligibility(Long applyId, Long recruitId,
                                                ApplyStatus status, SelectionResult selectionResult) {
        return new ApplyEligibilityFacts(applyId, recruitId, status, selectionResult);
    }

    @TestConfiguration
    static class TestDependencies {

        @Bean
        Map2JsonSerializer map2JsonSerializer() {
            return new Map2JsonSerializer(new ObjectMapper());
        }

        @Bean
        MailDispatchRequestFingerprintGenerator requestFingerprintGenerator(Map2JsonSerializer serializer) {
            return new MailDispatchRequestFingerprintGenerator(serializer);
        }
    }
}
