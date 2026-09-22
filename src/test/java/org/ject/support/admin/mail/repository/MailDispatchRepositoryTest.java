package org.ject.support.admin.mail.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.ject.support.admin.mail.domain.MailDispatchJob;
import org.ject.support.admin.mail.domain.MailDispatchJobStatus;
import org.ject.support.admin.mail.domain.MailDispatchTarget;
import org.ject.support.admin.mail.domain.MailDispatchTargetStatus;
import org.ject.support.testconfig.QueryDslTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

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
    @DisplayName("관리자와 모집 공고 조건으로 발송 작업을 페이지 조회한다")
    void 관리자와_모집_공고_조건으로_발송_작업을_페이지_조회한다() {
        // given
        MailDispatchJob first = mailDispatchJobRepository.save(
                MailDispatchJob.create(1L, 2L, 3L, "dispatch-key-1", "제목", "본문", "{}", 1));
        MailDispatchJob second = mailDispatchJobRepository.save(
                MailDispatchJob.create(1L, 2L, 3L, "dispatch-key-2", "제목", "본문", "{}", 1));
        mailDispatchJobRepository.save(
                MailDispatchJob.create(1L, 2L, 2L, "dispatch-key-3", "제목", "본문", "{}", 1));

        // when
        Page<MailDispatchJob> result = mailDispatchJobRepository
                .findAllByRequestedByAdminIdAndRecruitIdAndStatusOrderByRequestedAtDescIdDesc(
                        3L, 2L, MailDispatchJobStatus.REQUESTED, PageRequest.of(0, 1));

        // then
        assertThat(result.getContent()).containsExactly(second);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(mailDispatchJobRepository.findByIdAndRequestedByAdminId(first.getId(), 3L))
                .containsSame(first);
        assertThat(mailDispatchJobRepository.findByIdAndRequestedByAdminId(first.getId(), 2L))
                .isEmpty();
    }
}
