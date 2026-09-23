package org.ject.support.admin.mail.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import org.ject.support.admin.mail.domain.MailDispatchJob;
import org.ject.support.admin.mail.domain.MailDispatchJobStatus;
import org.ject.support.admin.mail.domain.MailDispatchTarget;
import org.ject.support.admin.mail.domain.MailDispatchTargetStatus;
import org.ject.support.admin.mail.dto.MailDispatchJobSearchCondition;
import org.ject.support.testconfig.QueryDslTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@Import(QueryDslTestConfig.class)
@DataJpaTest
class MailDispatchRepositoryTest {

    @Autowired
    private MailDispatchJobRepository mailDispatchJobRepository;

    @Autowired
    private MailDispatchTargetRepository mailDispatchTargetRepository;

    @Test
    @DisplayName("발송 작업과 대상 이력을 함께 저장하고 조회한다")
    void 발송_작업과_대상_이력을_함께_저장하고_조회한다() {
        // given
        MailDispatchJob job = mailDispatchJobRepository.saveAndFlush(
                MailDispatchJob.create(1L, 2L, 3L, "dispatch-key", "제목", "본문", "{}", 1));
        MailDispatchTarget target = mailDispatchTargetRepository.saveAndFlush(
                MailDispatchTarget.pending(job, 10L, "applicant@ject.kr"));

        // when
        List<MailDispatchTarget> targets = mailDispatchTargetRepository
                .findAllByDispatchJobIdOrderByIdAsc(job.getId());

        // then
        assertThat(targets).containsExactly(target);
        assertThat(targets.get(0).getStatus()).isEqualTo(MailDispatchTargetStatus.PENDING);
        assertThat(targets.get(0).getEmail()).isEqualTo("applicant@ject.kr");
        assertThat(mailDispatchTargetRepository
                .findByDispatchJobIdAndApplyId(job.getId(), 10L))
                .containsSame(target);
        assertThat(mailDispatchJobRepository
                .findByRequestedByAdminIdAndIdempotencyKey(3L, "dispatch-key"))
                .containsSame(job);
    }

    @Test
    @DisplayName("관리자와 조건 없이 발송 작업을 최신순 페이지 조회한다")
    void 관리자와_조건_없이_발송_작업을_최신순_페이지_조회한다() {
        // given
        MailDispatchJob oldest = saveJob(3L, 2L, "dispatch-key-oldest", LocalDateTime.of(2026, 9, 20, 10, 0));
        MailDispatchJob firstTie = saveJob(3L, 2L, "dispatch-key-first-tie", LocalDateTime.of(2026, 9, 21, 10, 0));
        MailDispatchJob secondTie = saveJob(3L, 2L, "dispatch-key-second-tie", LocalDateTime.of(2026, 9, 21, 10, 0));
        saveJob(2L, 2L, "dispatch-key-other-admin", LocalDateTime.of(2026, 9, 22, 10, 0));
        MailDispatchJob otherRecruit = saveJob(
                3L, 1L, "dispatch-key-other-recruit", LocalDateTime.of(2026, 9, 19, 10, 0));

        // when
        Page<MailDispatchJob> result = mailDispatchJobRepository.findJobs(
                3L, new MailDispatchJobSearchCondition(null, null), PageRequest.of(0, 2));
        Page<MailDispatchJob> nextPage = mailDispatchJobRepository.findJobs(
                3L, new MailDispatchJobSearchCondition(null, null), PageRequest.of(1, 2));

        // then
        assertThat(result.getContent()).containsExactly(secondTie, firstTie);
        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.hasNext()).isTrue();
        assertThat(nextPage.getContent()).containsExactly(oldest, otherRecruit);
        assertThat(nextPage.getNumber()).isEqualTo(1);
        assertThat(nextPage.hasNext()).isFalse();
        assertThat(oldest.getRequestedAt()).isBefore(firstTie.getRequestedAt());
    }

    @Test
    @DisplayName("모집 공고 조건만으로 발송 작업을 조회하면 해당 공고 결과만 반환한다")
    void 모집_공고_조건만으로_발송_작업을_조회하면_해당_공고_결과만_반환한다() {
        // given
        MailDispatchJob matching = saveJob(3L, 2L, "dispatch-key-recruit-matching", LocalDateTime.of(2026, 9, 22, 10, 0));
        saveJob(3L, 1L, "dispatch-key-recruit-other", LocalDateTime.of(2026, 9, 23, 10, 0));

        // when
        Page<MailDispatchJob> result = mailDispatchJobRepository.findJobs(
                3L, new MailDispatchJobSearchCondition(2L, null), PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).containsExactly(matching);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("작업 상태 조건만으로 발송 작업을 조회하면 해당 상태 결과만 반환한다")
    void 작업_상태_조건만으로_발송_작업을_조회하면_해당_상태_결과만_반환한다() {
        // given
        MailDispatchJob matching = saveCompletedJob(
                3L, 2L, "dispatch-key-status-matching", LocalDateTime.of(2026, 9, 22, 10, 0));
        saveJob(3L, 2L, "dispatch-key-status-other", LocalDateTime.of(2026, 9, 23, 10, 0));
        saveCompletedJob(2L, 2L, "dispatch-key-status-other-admin", LocalDateTime.of(2026, 9, 24, 10, 0));

        // when
        Page<MailDispatchJob> result = mailDispatchJobRepository.findJobs(
                3L, new MailDispatchJobSearchCondition(null, MailDispatchJobStatus.COMPLETED),
                PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).containsExactly(matching);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("모집 공고와 작업 상태 조건으로 발송 작업을 조회하면 두 조건을 모두 만족한 결과만 반환한다")
    void 모집_공고와_작업_상태_조건으로_발송_작업을_조회하면_두_조건을_모두_만족한_결과만_반환한다() {
        // given
        MailDispatchJob matching = saveCompletedJob(
                3L, 2L, "dispatch-key-both-matching", LocalDateTime.of(2026, 9, 22, 10, 0));
        saveJob(3L, 2L, "dispatch-key-both-status-other", LocalDateTime.of(2026, 9, 23, 10, 0));
        saveCompletedJob(3L, 1L, "dispatch-key-both-recruit-other", LocalDateTime.of(2026, 9, 24, 10, 0));

        // when
        Page<MailDispatchJob> result = mailDispatchJobRepository.findJobs(
                3L, new MailDispatchJobSearchCondition(2L, MailDispatchJobStatus.COMPLETED),
                PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).containsExactly(matching);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("발송 작업 소유자와 일치할 때만 상세를 조회한다")
    void 발송_작업_소유자와_일치할_때만_상세를_조회한다() {
        // given
        MailDispatchJob job = saveJob(3L, 2L, "dispatch-key-owner", LocalDateTime.of(2026, 9, 22, 10, 0));

        // when & then
        assertThat(mailDispatchJobRepository.findByIdAndRequestedByAdminId(job.getId(), 3L))
                .containsSame(job);
        assertThat(mailDispatchJobRepository.findByIdAndRequestedByAdminId(job.getId(), 2L))
                .isEmpty();
    }

    @Test
    @DisplayName("상태 조건이 없으면 해당 작업의 수신자 결과를 id 오름차순으로 페이지 조회한다")
    void 상태_조건이_없으면_해당_작업의_수신자_결과를_id_오름차순으로_페이지_조회한다() {
        // given
        MailDispatchJob job = saveJob(3L, 2L, "dispatch-key-targets", LocalDateTime.of(2026, 9, 22, 10, 0));
        MailDispatchTarget first = saveTarget(job, 10L, "first@ject.kr");
        MailDispatchTarget second = saveTarget(job, 11L, "second@ject.kr");
        MailDispatchTarget third = saveTarget(job, 12L, "third@ject.kr");
        MailDispatchJob otherJob = saveJob(
                3L, 2L, "dispatch-key-other-targets", LocalDateTime.of(2026, 9, 23, 10, 0));
        saveTarget(otherJob, 20L, "other@ject.kr");

        // when
        Page<MailDispatchTarget> result = mailDispatchTargetRepository.findTargets(
                job.getId(), null, PageRequest.of(0, 2));
        Page<MailDispatchTarget> nextPage = mailDispatchTargetRepository.findTargets(
                job.getId(), null, PageRequest.of(1, 2));

        // then
        assertThat(result.getContent()).containsExactly(first, second);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.hasNext()).isTrue();
        assertThat(nextPage.getContent()).containsExactly(third);
        assertThat(nextPage.getTotalElements()).isEqualTo(3);
        assertThat(nextPage.hasNext()).isFalse();
    }

    @Test
    @DisplayName("상태 조건이 있으면 해당 작업의 상태에 맞는 수신자 결과만 반환한다")
    void 상태_조건이_있으면_해당_작업의_상태에_맞는_수신자_결과만_반환한다() {
        // given
        MailDispatchJob job = saveJob(3L, 2L, "dispatch-key-target-status", LocalDateTime.of(2026, 9, 22, 10, 0));
        saveTarget(job, 10L, "pending@ject.kr");
        MailDispatchTarget sent = MailDispatchTarget.pending(job, 11L, "sent@ject.kr");
        sent.markSent();
        mailDispatchTargetRepository.saveAndFlush(sent);
        MailDispatchTarget failed = MailDispatchTarget.pending(job, 12L, "failed@ject.kr");
        failed.markFailed("MAIL-19");
        mailDispatchTargetRepository.saveAndFlush(failed);

        MailDispatchJob otherJob = saveJob(
                3L, 2L, "dispatch-key-other-target-status", LocalDateTime.of(2026, 9, 23, 10, 0));
        MailDispatchTarget otherSent = MailDispatchTarget.pending(otherJob, 20L, "other-sent@ject.kr");
        otherSent.markSent();
        mailDispatchTargetRepository.saveAndFlush(otherSent);

        // when
        Page<MailDispatchTarget> result = mailDispatchTargetRepository.findTargets(
                job.getId(), MailDispatchTargetStatus.SENT, PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).containsExactly(sent);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.hasNext()).isFalse();
    }

    private MailDispatchJob saveJob(Long adminId, Long recruitId, String idempotencyKey,
                                    LocalDateTime requestedAt) {
        MailDispatchJob job = MailDispatchJob.create(
                1L, recruitId, adminId, idempotencyKey, "제목", "본문", "{}", 1);
        ReflectionTestUtils.setField(job, "requestedAt", requestedAt);
        return mailDispatchJobRepository.saveAndFlush(job);
    }

    private MailDispatchJob saveCompletedJob(Long adminId, Long recruitId, String idempotencyKey,
                                             LocalDateTime requestedAt) {
        MailDispatchJob job = saveJob(adminId, recruitId, idempotencyKey, requestedAt);
        job.startProcessing();
        job.recordSuccess();
        return mailDispatchJobRepository.saveAndFlush(job);
    }

    private MailDispatchTarget saveTarget(MailDispatchJob job, Long applyId, String email) {
        return mailDispatchTargetRepository.saveAndFlush(MailDispatchTarget.pending(job, applyId, email));
    }
}
