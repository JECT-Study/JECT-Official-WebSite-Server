package org.ject.support.testconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import org.ject.support.admin.mail.config.MailDispatchWorkerConfig;
import org.ject.support.admin.mail.domain.MailDispatchJob;
import org.ject.support.admin.mail.repository.MailDispatchJobRepository;
import org.ject.support.admin.mail.service.MailDispatchClaimService;
import org.ject.support.admin.mail.service.MailDispatchExecutionService;
import org.ject.support.admin.mail.service.MailDispatchPersistenceService;
import org.ject.support.admin.mail.service.MailDispatchPlan;
import org.ject.support.common.config.JpaAuditConfig;
import org.ject.support.common.data.querydsl.QueryDslConfig;
import org.ject.support.common.response.ObjectMapperConfig;
import org.ject.support.common.util.Map2JsonSerializer;
import org.ject.support.external.email.domain.EmailTemplate;
import org.ject.support.external.email.service.EmailSendService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// 운영 컴포넌트 탐색에는 포함하지 않고 별도 JVM의 메일 실행 경계만 구성
@TestConfiguration(proxyBeanMethods = false)
@ImportAutoConfiguration({DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class,
        TransactionAutoConfiguration.class})
@EntityScan(basePackageClasses = MailDispatchJob.class)
@EnableJpaRepositories(basePackageClasses = MailDispatchJobRepository.class)
@Import({MailDispatchPersistenceService.class, MailDispatchClaimService.class,
        MailDispatchExecutionService.class, MailDispatchWorkerConfig.class,
        QueryDslConfig.class, JpaAuditConfig.class, Map2JsonSerializer.class, ObjectMapperConfig.class})
public class MailDispatchProcessTestApplication {

    public static void main(String[] args) throws Exception {
        String mode = args[0];
        if (!List.of("save-and-halt", "recover", "halt-after-accept").contains(mode)) {
            throw new IllegalArgumentException("Unsupported mail process test mode");
        }
        SpringApplication application = new SpringApplication(MailDispatchProcessTestApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.setDefaultProperties(Map.ofEntries(
                Map.entry("spring.datasource.url", System.getenv("MAIL_TEST_JDBC_URL")),
                Map.entry("spring.datasource.username", System.getenv("MAIL_TEST_DB_USER")),
                Map.entry("spring.datasource.password", System.getenv("MAIL_TEST_DB_PASSWORD")),
                Map.entry("spring.datasource.driver-class-name", "com.mysql.cj.jdbc.Driver"),
                Map.entry("spring.jpa.hibernate.ddl-auto", "validate"),
                Map.entry("spring.jpa.properties.hibernate.dialect", "org.hibernate.dialect.MySQLDialect"),
                Map.entry("spring.main.banner-mode", "off"),
                Map.entry("logging.level.root", "ERROR"),
                Map.entry("mail.dispatch.worker.enabled", !"save-and-halt".equals(mode)),
                Map.entry("mail.dispatch.worker.poll-delay", "100"),
                Map.entry("mail.process.test.halt-after-accept", "halt-after-accept".equals(mode))));
        // 환경별 운영 설정을 읽지 않고 부모가 만든 검증 DB 스키마를 그대로 유지
        var context = application.run("--spring.config.name=mail-process-test", "--spring.profiles.active=process-test");
        if ("save-and-halt".equals(mode)) {
            context.getBean(MailDispatchPersistenceService.class).createJob(new MailDispatchPlan(
                    1L, 2L, 3L, args[1], "제목", "본문", Map.of(),
                    List.of(new MailDispatchPlan.Target(10L, "recipient@example.com", "저장된 제목", "저장된 본문"))),
                    "process-fixture");
            // 저장 트랜잭션 커밋 직후 shutdown hook·finally 없이 프로세스 중단
            Runtime.getRuntime().halt(73);
        }
        new CountDownLatch(1).await();
    }

    @Bean
    EmailSendService processTestEmailSendService(ObjectMapper objectMapper,
            @Value("${mail.process.test.halt-after-accept:false}") boolean haltAfterAccept) {
        URI endpoint = URI.create(System.getenv("MAIL_TEST_ENDPOINT"));
        if (!"http".equals(endpoint.getScheme()) || !"127.0.0.1".equals(endpoint.getHost())) {
            throw new IllegalArgumentException("Mail process test requires a loopback endpoint");
        }
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        return new EmailSendService() {
            @Override
            public void sendEmail(String to, String subject, String htmlBody) {
                try {
                    HttpRequest request = HttpRequest.newBuilder(endpoint)
                            .timeout(Duration.ofSeconds(10))
                            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(
                                    Map.of("to", to, "subject", subject, "body", htmlBody))))
                            .build();
                    int status = client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
                    if (status != 204) {
                        throw new IllegalStateException("Local mail server did not accept the request");
                    }
                    if (haltAfterAccept) {
                        // 외부 수락은 끝났지만 운영 실행자의 DB 결과 기록 전인 장애 구간 검증
                        Runtime.getRuntime().halt(74);
                    }
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Local mail request interrupted", exception);
                } catch (IOException exception) {
                    throw new IllegalStateException("Local mail request failed", exception);
                }
            }

            @Override
            public void sendTemplatedEmail(EmailTemplate template, String to, Map<String, String> params) {
                throw new UnsupportedOperationException("Only saved mail snapshots are supported in this test");
            }

            @Override
            public void sendBulkTemplatedEmail(EmailTemplate template, List<String> recipients, Map<String, String> params) {
                throw new UnsupportedOperationException("Only saved mail snapshots are supported in this test");
            }
        };
    }
}
