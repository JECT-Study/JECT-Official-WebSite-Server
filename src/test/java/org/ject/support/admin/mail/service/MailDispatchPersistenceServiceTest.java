package org.ject.support.admin.mail.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;
import org.ject.support.admin.mail.domain.MailDispatchJob;
import org.ject.support.admin.mail.domain.MailDispatchTarget;
import org.ject.support.admin.mail.repository.MailDispatchJobRepository;
import org.ject.support.admin.mail.repository.MailDispatchTargetRepository;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.common.util.Map2JsonSerializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

class MailDispatchPersistenceServiceTest extends UnitTestSupport {

    @Mock
    private MailDispatchJobRepository mailDispatchJobRepository;

    @Mock
    private MailDispatchTargetRepository mailDispatchTargetRepository;

    @Mock
    private Map2JsonSerializer map2JsonSerializer;

    @InjectMocks
    private MailDispatchPersistenceService mailDispatchPersistenceService;

    @Test
    @DisplayName("발송 작업을 저장할 때 템플릿과 대상 이메일을 스냅샷으로 저장한다")
    @SuppressWarnings("unchecked")
    void 발송_작업을_저장할_때_템플릿과_대상_이메일을_스냅샷으로_저장한다() {
        // given
        MailDispatchPlan plan = new MailDispatchPlan(
                1L,
                2L,
                3L,
                "dispatch-key",
                "제목 템플릿",
                "본문 템플릿",
                Map.of("MESSAGE", "안내"),
                List.of(new MailDispatchPlan.Target(10L, "applicant@ject.kr", "제목", "본문")));
        given(map2JsonSerializer.serializeAsString(plan.inputVariables())).willReturn("{\"MESSAGE\":\"안내\"}");
        given(mailDispatchJobRepository.save(any(MailDispatchJob.class))).willAnswer(invocation -> {
            MailDispatchJob job = invocation.getArgument(0);
            ReflectionTestUtils.setField(job, "id", 100L);
            return job;
        });

        // when
        MailDispatchJob savedJob = mailDispatchPersistenceService.createJob(plan);

        // then
        assertThat(savedJob.getScenarioId()).isEqualTo(1L);
        assertThat(savedJob.getRecruitId()).isEqualTo(2L);
        assertThat(savedJob.getRequestedByAdminId()).isEqualTo(3L);
        assertThat(savedJob.getIdempotencyKey()).isEqualTo("dispatch-key");
        assertThat(savedJob.getSubjectTemplate()).isEqualTo("제목 템플릿");
        assertThat(savedJob.getBodyTemplate()).isEqualTo("본문 템플릿");
        assertThat(savedJob.getInputVariablesJson()).isEqualTo("{\"MESSAGE\":\"안내\"}");
        assertThat(savedJob.getTargetCount()).isEqualTo(1);

        ArgumentCaptor<List<MailDispatchTarget>> captor = ArgumentCaptor.forClass(List.class);
        verify(mailDispatchTargetRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).singleElement().satisfies(target -> {
            assertThat(target.getApplyId()).isEqualTo(10L);
            assertThat(target.getEmail()).isEqualTo("applicant@ject.kr");
            assertThat(target.getDispatchJob()).isSameAs(savedJob);
        });
    }
}
