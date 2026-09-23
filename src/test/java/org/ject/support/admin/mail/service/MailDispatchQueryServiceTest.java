package org.ject.support.admin.mail.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Optional;
import org.ject.support.admin.mail.domain.MailDispatchJob;
import org.ject.support.admin.mail.domain.MailDispatchJobStatus;
import org.ject.support.admin.mail.domain.MailDispatchTarget;
import org.ject.support.admin.mail.domain.MailDispatchTargetStatus;
import org.ject.support.admin.mail.dto.MailDispatchJobResponse;
import org.ject.support.admin.mail.dto.MailDispatchJobSearchCondition;
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
        given(mailDispatchJobRepository.findJobs(
                adminId, new MailDispatchJobSearchCondition(null, null), pageable))
                .willReturn(new PageImpl<>(List.of(job), pageable, 1));

        // when
        Page<MailDispatchJobResponse> result = mailDispatchQueryService.searchJobs(
                adminId, new MailDispatchJobSearchCondition(null, null), pageable);

        // then
        assertThat(result.getContent()).singleElement().satisfies(response -> {
            assertThat(response.dispatchJobId()).isEqualTo(100L);
            assertThat(response.requestedByAdminId()).isEqualTo(adminId);
            assertThat(response.status()).isEqualTo(MailDispatchJobStatus.REQUESTED);
        });
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getNumber()).isZero();
    }

    @Test
    @DisplayName("모집 공고와 작업 상태로 발송 작업을 필터링한다")
    void 모집_공고와_작업_상태로_발송_작업을_필터링한다() {
        // given
        Long adminId = 50L;
        PageRequest pageable = PageRequest.of(1, 10);
        MailDispatchJob job = createJob(100L, adminId);
        given(mailDispatchJobRepository.findJobs(
                adminId, new MailDispatchJobSearchCondition(2L, MailDispatchJobStatus.COMPLETED), pageable))
                .willReturn(new PageImpl<>(List.of(job), pageable, 11));

        // when
        Page<MailDispatchJobResponse> result = mailDispatchQueryService.searchJobs(
                adminId, new MailDispatchJobSearchCondition(2L, MailDispatchJobStatus.COMPLETED), pageable);

        // then
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getTotalElements()).isEqualTo(11);
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("모집 공고만으로 발송 작업을 필터링하면 조건에 맞는 결과를 반환한다")
    void 모집_공고만으로_발송_작업을_필터링하면_조건에_맞는_결과를_반환한다() {
        // given
        PageRequest pageable = PageRequest.of(0, 10);
        given(mailDispatchJobRepository.findJobs(
                50L, new MailDispatchJobSearchCondition(2L, null), pageable))
                .willReturn(Page.empty(pageable));

        // when
        Page<MailDispatchJobResponse> result = mailDispatchQueryService.searchJobs(
                50L, new MailDispatchJobSearchCondition(2L, null), pageable);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("작업 상태만으로 발송 작업을 필터링하면 조건에 맞는 결과를 반환한다")
    void 작업_상태만으로_발송_작업을_필터링하면_조건에_맞는_결과를_반환한다() {
        // given
        PageRequest pageable = PageRequest.of(0, 10);
        given(mailDispatchJobRepository.findJobs(
                50L, new MailDispatchJobSearchCondition(null, MailDispatchJobStatus.FAILED), pageable))
                .willReturn(Page.empty(pageable));

        // when
        Page<MailDispatchJobResponse> result = mailDispatchQueryService.searchJobs(
                50L, new MailDispatchJobSearchCondition(null, MailDispatchJobStatus.FAILED), pageable);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("다른 관리자의 발송 작업 상세 조회를 존재하지 않는 작업처럼 거부한다")
    void 다른_관리자의_발송_작업_상세_조회를_존재하지_않는_작업처럼_거부한다() {
        // given
        given(mailDispatchJobRepository.findByIdAndRequestedByAdminId(100L, 50L))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> mailDispatchQueryService.getJob(50L, 100L))
                .isInstanceOf(MailException.class)
                .extracting(exception -> ((MailException) exception).getErrorCode())
                .isEqualTo(MailErrorCode.DISPATCH_JOB_NOT_FOUND);
    }

    @Test
    @DisplayName("존재하지 않는 발송 작업 상세 조회를 not found로 처리한다")
    void 존재하지_않는_발송_작업_상세_조회를_not_found로_처리한다() {
        // given
        given(mailDispatchJobRepository.findByIdAndRequestedByAdminId(999L, 50L))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> mailDispatchQueryService.getJob(50L, 999L))
                .isInstanceOf(MailException.class)
                .hasFieldOrPropertyWithValue("errorCode", MailErrorCode.DISPATCH_JOB_NOT_FOUND);
    }

    @Test
    @DisplayName("발송 작업 소유자의 수신자별 결과를 상태로 필터링해 조회한다")
    void 발송_작업_소유자의_수신자별_결과를_상태로_필터링해_조회한다() {
        // given
        Long adminId = 50L;
        Long jobId = 100L;
        PageRequest pageable = PageRequest.of(0, 10);
        MailDispatchJob job = createJob(jobId, adminId);
        MailDispatchTarget target = MailDispatchTarget.pending(job, 10L, "applicant@ject.kr");
        target.markSent();
        given(mailDispatchJobRepository.findByIdAndRequestedByAdminId(jobId, adminId))
                .willReturn(Optional.of(job));
        given(mailDispatchTargetRepository.findTargets(jobId, MailDispatchTargetStatus.SENT, pageable))
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
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("다른 관리자는 발송 작업의 수신자별 결과를 조회할 수 없다")
    void 다른_관리자는_발송_작업의_수신자별_결과를_조회할_수_없다() {
        // given
        given(mailDispatchJobRepository.findByIdAndRequestedByAdminId(100L, 50L))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> mailDispatchQueryService.searchTargets(
                50L, 100L, null, PageRequest.of(0, 10)))
                .isInstanceOf(MailException.class)
                .hasFieldOrPropertyWithValue("errorCode", MailErrorCode.DISPATCH_JOB_NOT_FOUND);
    }

    private MailDispatchJob createJob(Long jobId, Long adminId) {
        MailDispatchJob job = MailDispatchJob.create(
                1L, 2L, adminId, "key-" + jobId, "제목", "본문", "{}", 1);
        org.springframework.test.util.ReflectionTestUtils.setField(job, "id", jobId);
        return job;
    }
}
