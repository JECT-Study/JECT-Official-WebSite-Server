package org.ject.support.admin.mail.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import org.ject.support.admin.mail.domain.MailDispatchJob;
import org.ject.support.admin.mail.domain.MailDispatchJobStatus;
import org.ject.support.admin.mail.domain.MailDispatchOutboxStatus;
import org.ject.support.admin.mail.domain.MailDispatchTargetStatus;
import org.ject.support.admin.mail.dto.MailDispatchResponse;
import org.ject.support.admin.mail.dto.SendMailDispatchRequest;
import org.ject.support.admin.mail.exception.MailErrorCode;
import org.ject.support.admin.mail.exception.MailException;
import org.ject.support.admin.mail.repository.MailDispatchJobRepository;
import org.ject.support.admin.mail.repository.MailDispatchOutboxRepository;
import org.ject.support.base.TestSupport;
import org.ject.support.common.util.Map2JsonSerializer;
import org.ject.support.external.email.exception.EmailErrorCode;
import org.ject.support.external.email.exception.EmailException;
import org.ject.support.external.email.service.EmailSendService;
import org.ject.support.testconfig.QueryDslTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
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
@Import({
        MailDispatchPersistenceService.class,
        MailDispatchUseCase.class,
        MailDispatchQueryService.class,
        QueryDslTestConfig.class,
        MailDispatchUseCaseMysqlIntegrationTest.TestDependencies.class
})
@Testcontainers
class MailDispatchUseCaseMysqlIntegrationTest extends TestSupport {

    @Container
    private static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.2");

    @Autowired
    private MailDispatchUseCase mailDispatchUseCase;

    @Autowired
    private MailDispatchJobRepository mailDispatchJobRepository;

    @Autowired
    private MailDispatchOutboxRepository outboxRepository;

    @Autowired
    private MailDispatchQueryService queryService;

    @MockitoBean
    private MailDispatchPreparationService preparationService;

    @MockitoBean
    private EmailSendService emailSendService;

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.driver-class-name", mysqlContainer::getDriverClassName);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.MySQLDialect");
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
