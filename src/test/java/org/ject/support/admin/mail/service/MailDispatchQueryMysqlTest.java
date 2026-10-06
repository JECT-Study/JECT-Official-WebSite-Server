package org.ject.support.admin.mail.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.ject.support.admin.mail.domain.MailDispatchJob;
import org.ject.support.admin.mail.domain.MailDispatchJobStatus;
import org.ject.support.admin.mail.domain.MailDispatchOutbox;
import org.ject.support.admin.mail.domain.MailDispatchTarget;
import org.ject.support.admin.mail.domain.MailDispatchTargetStatus;
import org.ject.support.admin.mail.dto.MailDispatchJobResponse;
import org.ject.support.admin.mail.dto.MailDispatchJobSearchCondition;
import org.ject.support.admin.mail.dto.MailDispatchTargetResponse;
import org.ject.support.admin.mail.exception.MailErrorCode;
import org.ject.support.admin.mail.exception.MailException;
import org.ject.support.admin.mail.repository.MailDispatchJobRepository;
import org.ject.support.admin.mail.repository.MailDispatchOutboxRepository;
import org.ject.support.admin.mail.repository.MailDispatchTargetRepository;
import org.ject.support.base.TestSupport;
import org.ject.support.common.response.ObjectMapperConfig;
import org.ject.support.common.util.Map2JsonSerializer;
import org.ject.support.external.email.exception.EmailErrorCode;
import org.ject.support.testconfig.QueryDslTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
@Import({MailDispatchPersistenceService.class, MailDispatchClaimService.class, MailDispatchQueryService.class,
        QueryDslTestConfig.class, ObjectMapperConfig.class, Map2JsonSerializer.class})
@Testcontainers
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class MailDispatchQueryMysqlTest extends TestSupport {

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
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.driver-class-name", MYSQL::getDriverClassName);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.MySQLDialect");
    }

    @BeforeEach
    void 발송_조회_테스트_데이터_정리() {
        outboxRepository.deleteAllInBatch();
        targetRepository.deleteAllInBatch();
        jobRepository.deleteAllInBatch();
    }

    @Test
    void 타_관리자의_작업과_없는_작업은_상세와_대상_조회에서_같은_오류로_거부한다() {
        // given
        var job = createJob(3L, 2L, 10L);

        // when & then
        for (Long inaccessibleId : List.of(job.getId(), Long.MAX_VALUE)) {
            assertThatThrownBy(() -> queryService.getJob(4L, inaccessibleId))
                    .isInstanceOf(MailException.class)
                    .extracting("errorCode").isEqualTo(MailErrorCode.DISPATCH_JOB_NOT_FOUND);
            assertThatThrownBy(() -> queryService.searchTargets(
                    4L, inaccessibleId, null, PageRequest.of(0, 10)))
                    .isInstanceOf(MailException.class)
                    .extracting("errorCode").isEqualTo(MailErrorCode.DISPATCH_JOB_NOT_FOUND);
        }
        assertThat(queryService.getJob(3L, job.getId()).requestedByAdminId()).isEqualTo(3L);
    }

    @Test
    void 과거_이력과_미시도_대상을_구분하고_다른_작업의_재시도_정보를_섞지_않는다() {
        // given
        var legacy = jobRepository.save(MailDispatchJob.create(
                1L, 2L, 3L, "legacy-key", "과거 제목", "과거 본문", "{}", 2));
        targetRepository.save(MailDispatchTarget.pending(legacy, 10L, "legacy@example.com"));
        targetRepository.save(MailDispatchTarget.pending(legacy, 20L, "pending@example.com"));
        outboxRepository.save(MailDispatchOutbox.createPending(
                legacy, 20L, "pending@example.com", "제목", "본문"));
        persistenceService.startProcessing(legacy.getId());
        persistenceService.recordSuccess(legacy.getId(), 10L);
        var otherJob = createJob(4L, 99L, 20L);
        LocalDateTime now = LocalDateTime.of(2026, 10, 5, 12, 0);
        var otherClaim = claimService.claim(outboxId(otherJob.getId(), 20L), now, Duration.ofMinutes(2))
                .orElseThrow();
        assertThat(claimService.recordThrottling(
                outboxId(otherJob.getId(), 20L), otherClaim.claimToken(), now.plusSeconds(1))).isTrue();

        // when
        var legacyResult = queryService.searchTargets(
                3L, legacy.getId(), MailDispatchTargetStatus.SENT, PageRequest.of(0, 10));
        var first = queryService.searchTargets(3L, legacy.getId(), null, PageRequest.of(0, 1));
        var second = queryService.searchTargets(3L, legacy.getId(), null, PageRequest.of(1, 1));

        // then
        assertThat(legacyResult.getTotalElements()).isEqualTo(1);
        assertThat(legacyResult.getContent()).singleElement().satisfies(target -> {
            assertThat(target.email()).isEqualTo("legacy@example.com");
            assertThat(target.status()).isEqualTo(MailDispatchTargetStatus.SENT);
            assertThat(target.sentAt()).isNotNull();
            assertThat(target.attemptCount()).isNull();
            assertThat(target.nextAttemptAt()).isNull();
            assertThat(target.lastAttemptFailureReason()).isNull();
        });
        assertThat(first.getTotalElements()).isEqualTo(2);
        assertThat(first.getContent()).extracting(MailDispatchTargetResponse::applyId).containsExactly(10L);
        assertThat(second.getTotalElements()).isEqualTo(2);
        assertThat(second.getContent()).singleElement().satisfies(target -> {
            assertThat(target.applyId()).isEqualTo(20L);
            assertThat(target.status()).isEqualTo(MailDispatchTargetStatus.PENDING);
            assertThat(target.attemptCount()).isZero();
            assertThat(target.nextAttemptAt()).isNull();
            assertThat(target.lastAttemptFailureReason()).isNull();
        });
        assertThat(queryService.searchTargets(3L, legacy.getId(), null, PageRequest.of(2, 1)).getContent()).isEmpty();
    }

    @Test
    void 같은_요청_시각의_본인_작업을_ID_내림차순으로_페이지_조회한다() {
        // given
        var first = createJob(3L, 2L, 10L);
        var second = createJob(3L, 2L, 20L);
        var third = createJob(3L, 99L, 30L);
        createJob(4L, 2L, 40L);
        jdbcTemplate.update("UPDATE mail_dispatch_job SET requested_at = ?",
                LocalDateTime.of(2026, 10, 5, 12, 0));

        // when
        var page0 = queryService.searchJobs(
                3L, new MailDispatchJobSearchCondition(null, null), PageRequest.of(0, 2));
        var page1 = queryService.searchJobs(
                3L, new MailDispatchJobSearchCondition(null, null), PageRequest.of(1, 2));
        var filtered = queryService.searchJobs(
                3L, new MailDispatchJobSearchCondition(2L, MailDispatchJobStatus.REQUESTED), PageRequest.of(0, 10));

        // then
        assertThat(page0.getContent()).extracting(MailDispatchJobResponse::dispatchJobId)
                .containsExactly(third.getId(), second.getId());
        assertThat(page0.getTotalElements()).isEqualTo(3);
        assertThat(page0.hasNext()).isTrue();
        assertThat(page1.getContent()).extracting(MailDispatchJobResponse::dispatchJobId)
                .containsExactly(first.getId());
        assertThat(page1.getTotalElements()).isEqualTo(3);
        assertThat(page1.hasNext()).isFalse();
        assertThat(filtered.getContent()).extracting(MailDispatchJobResponse::dispatchJobId)
                .containsExactly(second.getId(), first.getId());
        assertThat(filtered.getTotalElements()).isEqualTo(2);
    }

    @ParameterizedTest
    @EnumSource(MailDispatchTargetStatus.class)
    void 대상_상태별_페이지와_전체_건수에_다른_작업의_같은_지원_ID를_섞지_않는다(
            MailDispatchTargetStatus status) {
        // given
        var job = createJob(3L, 2L, 30L, 10L, 50L, 20L, 40L);
        createJob(4L, 99L, 10L, 20L, 30L, 40L, 50L);
        LocalDateTime now = LocalDateTime.of(2026, 10, 5, 12, 0);
        var sent = claimService.claim(outboxId(job.getId(), 20L), now, Duration.ofMinutes(2)).orElseThrow();
        claimService.recordSuccess(outboxId(job.getId(), 20L), sent.claimToken(), now.plusSeconds(1));
        var failed = claimService.claim(outboxId(job.getId(), 30L), now, Duration.ofMinutes(2)).orElseThrow();
        claimService.recordFailure(outboxId(job.getId(), 30L), failed.claimToken(), now.plusSeconds(1),
                EmailErrorCode.EMAIL_SEND_FAILURE);
        for (Long applyId : List.of(40L, 50L)) {
            var unknown = claimService.claim(outboxId(job.getId(), applyId), now, Duration.ofMinutes(2)).orElseThrow();
            claimService.recordUnknown(outboxId(job.getId(), applyId), unknown.claimToken());
        }

        // when
        var page0 = queryService.searchTargets(3L, job.getId(), status, PageRequest.of(0, 1));
        var page1 = queryService.searchTargets(3L, job.getId(), status, PageRequest.of(1, 1));

        // then
        Long firstApplyId = switch (status) {
            case PENDING -> 10L;
            case SENT -> 20L;
            case FAILED -> 30L;
            case UNKNOWN -> 50L;
        };
        assertThat(page0.getContent()).singleElement().satisfies(target -> {
            assertThat(target.applyId()).isEqualTo(firstApplyId);
            assertThat(target.status()).isEqualTo(status);
            assertThat(target.attemptCount()).isEqualTo(status == MailDispatchTargetStatus.PENDING ? 0 : 1);
            if (status == MailDispatchTargetStatus.FAILED) {
                assertThat(target.failureReason()).isEqualTo("EMAIL_SEND_FAILURE");
                assertThat(target.lastAttemptFailureReason()).isEqualTo("EMAIL_SEND_FAILURE");
                assertThat(target.sentAt()).isNull();
            } else if (status == MailDispatchTargetStatus.UNKNOWN) {
                assertThat(target.failureReason()).isEqualTo("MAIL-21");
                assertThat(target.lastAttemptFailureReason()).isEqualTo("MAIL-21");
                assertThat(target.sentAt()).isNull();
            } else {
                assertThat(target.failureReason()).isNull();
                assertThat(target.lastAttemptFailureReason()).isNull();
            }
        });
        assertThat(page0.getTotalElements()).isEqualTo(status == MailDispatchTargetStatus.UNKNOWN ? 2 : 1);
        if (status == MailDispatchTargetStatus.UNKNOWN) {
            assertThat(page0.hasNext()).isTrue();
            assertThat(page1.getContent()).extracting(MailDispatchTargetResponse::applyId).containsExactly(40L);
            assertThat(page1.getTotalElements()).isEqualTo(2);
        } else {
            assertThat(page0.hasNext()).isFalse();
            assertThat(page1.getContent()).isEmpty();
            assertThat(page1.getTotalElements()).isEqualTo(1);
        }
        assertThat(page1.hasNext()).isFalse();
    }

    @Test
    void 재시도_대기는_최종_실패와_구분하고_다음_시각과_마지막_오류를_조회한다() {
        // given
        var job = createJob(3L, 2L, 10L);
        LocalDateTime now = LocalDateTime.of(2026, 10, 5, 12, 0);
        var claim = claimService.claim(outboxId(job.getId(), 10L), now, Duration.ofMinutes(2)).orElseThrow();
        claimService.recordThrottling(outboxId(job.getId(), 10L), claim.claimToken(), now.plusSeconds(1));

        // when
        var detail = queryService.getJob(3L, job.getId());
        var targets = queryService.searchTargets(
                3L, job.getId(), MailDispatchTargetStatus.PENDING, PageRequest.of(0, 10));
        var failures = queryService.searchTargets(
                3L, job.getId(), MailDispatchTargetStatus.FAILED, PageRequest.of(0, 10));

        // then
        assertThat(detail.status()).isEqualTo(MailDispatchJobStatus.PROCESSING);
        assertThat(detail.processingCount()).isEqualTo(1);
        assertThat(detail.failedCount()).isZero();
        assertThat(targets.getContent()).singleElement().satisfies(target -> {
            assertThat(target.attemptCount()).isEqualTo(1);
            assertThat(target.nextAttemptAt()).isEqualTo(LocalDateTime.of(2026, 10, 5, 12, 0, 31));
            assertThat(target.failureReason()).isNull();
            assertThat(target.lastAttemptFailureReason()).isEqualTo("TOO_MANY_EMAIL_REQUESTS");
            assertThat(target.sentAt()).isNull();
        });
        assertThat(failures.getContent()).isEmpty();
        assertThat(failures.getTotalElements()).isZero();
    }

    @Test
    void 조회_응답에_저장된_본문과_제목과_claim_token을_노출하지_않는다() throws Exception {
        // given
        var job = createJob(3L, 2L, 10L);
        var claim = claimService.claim(outboxId(job.getId(), 10L),
                LocalDateTime.of(2026, 10, 5, 12, 0), Duration.ofMinutes(2)).orElseThrow();

        // when
        var detail = objectMapper.writeValueAsString(queryService.getJob(3L, job.getId()));
        var targets = objectMapper.writeValueAsString(queryService.searchTargets(
                3L, job.getId(), null, PageRequest.of(0, 10)).getContent());

        // then
        assertThat(detail).contains("\"dispatchJobId\":" + job.getId())
                .doesNotContain("비공개 제목", "비공개 본문", "subjectTemplate", "bodyTemplate", claim.claimToken());
        assertThat(targets).contains("10@example.com", "\"attemptCount\":1")
                .doesNotContain("비공개 제목", "비공개 본문", "\"subject\"", "\"body\"", "claimToken",
                        "leaseUntil", claim.claimToken());
    }

    private Long outboxId(Long jobId, Long applyId) {
        return outboxRepository.findByDispatchJobIdAndApplyId(jobId, applyId).orElseThrow().getId();
    }

    private MailDispatchJob createJob(Long adminId, Long recruitId, Long... applyIds) {
        return persistenceService.createJob(new MailDispatchPlan(
                1L, recruitId, adminId, UUID.randomUUID().toString(), "비공개 제목", "비공개 본문", Map.of(),
                List.of(applyIds).stream().map(applyId -> new MailDispatchPlan.Target(
                        applyId, applyId + "@example.com", "비공개 제목", "비공개 본문")).toList()), "fixture");
    }
}
