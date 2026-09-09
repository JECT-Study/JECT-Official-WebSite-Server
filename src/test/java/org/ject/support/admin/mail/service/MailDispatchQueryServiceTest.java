package org.ject.support.admin.mail.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.List;
import org.ject.support.admin.mail.domain.MailDispatchJob;
import org.ject.support.admin.mail.domain.MailDispatchJobStatus;
import org.ject.support.admin.mail.domain.MailDispatchTarget;
import org.ject.support.admin.mail.domain.MailDispatchTargetStatus;
import org.ject.support.admin.mail.dto.MailDispatchJobResponse;
import org.ject.support.admin.mail.dto.MailDispatchTargetResponse;
import org.ject.support.admin.mail.exception.MailErrorCode;
import org.ject.support.admin.mail.exception.MailException;
import org.ject.support.admin.mail.repository.MailDispatchJobRepository;
import org.ject.support.admin.mail.repository.MailDispatchTargetRepository;
import org.ject.support.base.UnitTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class MailDispatchQueryServiceTest extends UnitTestSupport {

    @Mock
    private MailDispatchJobRepository mailDispatchJobRepository;

    @Mock
    private MailDispatchTargetRepository mailDispatchTargetRepository;

    @InjectMocks
    private MailDispatchQueryService mailDispatchQueryService;

    @Test
    @DisplayName("관리자 본인의 발송 작업 목록을 최신순 페이지로 조회한다")
    void 관리자_본인의_발송_작업_목록을_최신순_페이지로_조회한다() {
        // given
        Long adminId = 50L;
        PageRequest pageable = PageRequest.of(0, 10);
        MailDispatchJob job = createJob(100L, adminId);
        given(mailDispatchJobRepository.findAllByRequestedByAdminIdOrderByRequestedAtDescIdDesc(adminId, pageable))
                .willReturn(new PageImpl<>(List.of(job), pageable, 1));

        // when
        Page<MailDispatchJobResponse> result = mailDispatchQueryService.searchJobs(adminId, null, null, pageable);

        // then
        assertThat(result.getContent()).singleElement().satisfies(response -> {
            assertThat(response.dispatchJobId()).isEqualTo(100L);
            assertThat(response.requestedByAdminId()).isEqualTo(adminId);
            assertThat(response.status()).isEqualTo(MailDispatchJobStatus.REQUESTED);
        });
    }

    @Test
    @DisplayName("모집 공고와 작업 상태로 발송 작업을 필터링한다")
    void 모집_공고와_작업_상태로_발송_작업을_필터링한다() {
        // given
        Long adminId = 50L;
        Long recruitId = 2L;
        PageRequest pageable = PageRequest.of(1, 10);
        MailDispatchJob job = createJob(100L, adminId);
        given(mailDispatchJobRepository.findAllByRequestedByAdminIdAndRecruitIdAndStatusOrderByRequestedAtDescIdDesc(
                adminId, recruitId, MailDispatchJobStatus.COMPLETED, pageable))
                .willReturn(new PageImpl<>(List.of(job), pageable, 11));

        // when
        Page<MailDispatchJobResponse> result = mailDispatchQueryService.searchJobs(
                adminId, recruitId, MailDispatchJobStatus.COMPLETED, pageable);

        // then
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getTotalElements()).isEqualTo(11);
    }

    @Test
    @DisplayName("다른 관리자의 발송 작업 상세 조회를 거부한다")
    void 다른_관리자의_발송_작업_상세_조회를_거부한다() {
        // given
        given(mailDispatchJobRepository.findByIdAndRequestedByAdminId(100L, 50L))
                .willReturn(java.util.Optional.empty());

        // when & then
        assertThatThrownBy(() -> mailDispatchQueryService.getJob(50L, 100L))
                .isInstanceOf(MailException.class)
                .extracting("errorCode")
                .isEqualTo(MailErrorCode.DISPATCH_JOB_NOT_FOUND);
    }

    @Test
    @DisplayName("발송 작업의 수신자별 결과를 상태로 필터링해 조회한다")
    void 발송_작업의_수신자별_결과를_상태로_필터링해_조회한다() {
        // given
        Long adminId = 50L;
        Long jobId = 100L;
        PageRequest pageable = PageRequest.of(0, 10);
        MailDispatchJob job = createJob(jobId, adminId);
        MailDispatchTarget target = MailDispatchTarget.pending(job, 10L, "applicant@ject.kr");
        target.markSent();
        given(mailDispatchJobRepository.findByIdAndRequestedByAdminId(jobId, adminId)).willReturn(java.util.Optional.of(job));
        given(mailDispatchTargetRepository.findAllByDispatchJobIdAndStatusOrderByIdAsc(
                jobId, MailDispatchTargetStatus.SENT, pageable))
                .willReturn(new PageImpl<>(List.of(target), pageable, 1));

        // when
        Page<MailDispatchTargetResponse> result = mailDispatchQueryService.searchTargets(
                adminId, jobId, MailDispatchTargetStatus.SENT, pageable);

        // then
        assertThat(result.getContent()).singleElement().satisfies(response -> {
            assertThat(response.applyId()).isEqualTo(10L);
            assertThat(response.email()).isEqualTo("applicant@ject.kr");
            assertThat(response.status()).isEqualTo(MailDispatchTargetStatus.SENT);
        });
    }

    private MailDispatchJob createJob(Long jobId, Long adminId) {
        MailDispatchJob job = MailDispatchJob.create(
                1L, 2L, adminId, "key-" + jobId, "제목", "본문", "{}", 1);
        org.springframework.test.util.ReflectionTestUtils.setField(job, "id", jobId);
        return job;
    }
}
