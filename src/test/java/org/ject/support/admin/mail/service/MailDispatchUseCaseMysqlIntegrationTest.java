package org.ject.support.admin.mail.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.ject.support.admin.mail.domain.MailDispatchJob;
import org.ject.support.admin.mail.dto.SendMailDispatchRequest;
import org.ject.support.admin.mail.exception.MailErrorCode;
import org.ject.support.admin.mail.exception.MailException;
import org.ject.support.admin.mail.repository.MailDispatchJobRepository;
import org.ject.support.base.TestSupport;
import org.ject.support.common.util.Map2JsonSerializer;
import org.ject.support.external.email.service.EmailSendService;
import org.ject.support.testconfig.QueryDslTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
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
