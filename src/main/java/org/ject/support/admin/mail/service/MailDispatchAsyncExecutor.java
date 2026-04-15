package org.ject.support.admin.mail.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ject.support.admin.mail.domain.MailDispatchJob;
import org.ject.support.admin.mail.domain.MailDispatchJobRepository;
import org.ject.support.admin.mail.domain.MailDispatchTarget;
import org.ject.support.admin.mail.domain.MailDispatchTargetRepository;
import org.ject.support.admin.mail.domain.MailScenario;
import org.ject.support.common.util.String2MapSerializer;
import org.ject.support.external.email.service.EmailSendService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 대량 메일 발송 작업을 비동기적으로 수행하는 엑스큐터입니다.
 * 별도의 컴포넌트로 분리하여 MailDispatchService와의 순환 참조를 방지하고 @Async가 정상 동작하도록 합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MailDispatchAsyncExecutor {

    private final MailDispatchJobRepository jobRepository;
    private final MailDispatchTargetRepository targetRepository;
    private final MailTemplateEngine templateEngine;
    private final EmailSendService emailSendService;
    private final String2MapSerializer string2MapSerializer;

    /**
     * 지정된 발송 작업을 백그라운드에서 실행합니다.
     *
     * @param jobId 실행할 발송 작업 ID
     */
    @Async
    @Transactional
    public void execute(Long jobId) {
        MailDispatchJob job = jobRepository.findByIdWithScenario(jobId)
                .orElseThrow(() -> {
                    log.error("비동기 발송 작업 시작 실패: 작업을 찾을 수 없습니다. jobId={}", jobId);
                    return new IllegalArgumentException("발송 작업을 찾을 수 없습니다.");
                });

        log.info("비동기 메일 발송 시작: jobId={}, scenario={}", jobId, job.getScenario().getName());
        job.markProcessing();

        List<MailDispatchTarget> targets = targetRepository.findAllByJobIdOrderByIdAsc(jobId);
        Map<String, String> commonVariables = string2MapSerializer.serializeAsMap(job.getCommonVariablesJson());
        MailScenario scenario = job.getScenario();

        for (MailDispatchTarget target : targets) {
            dispatchToTarget(scenario, target, commonVariables);
        }

        job.markCompleted();
        log.info("비동기 메일 발송 완료: jobId={}", jobId);
    }

    private void dispatchToTarget(MailScenario scenario, MailDispatchTarget target, Map<String, String> commonVariables) {
        try {
            // TODO: 수신자별 개별 변수(Personal Variables)가 도입될 경우 여기서 병합 로직 추가
            Map<String, Object> variables = (Map) commonVariables; 

            String subject = templateEngine.render(scenario.getSubjectTemplate(), variables);
            String body = templateEngine.render(scenario.getBodyTemplate(), variables);

            emailSendService.sendEmail(target.getEmail(), subject, body);
            target.markSent();
        } catch (Exception e) {
            log.error("개별 대상 메일 발송 실패: targetId={}, email={}", target.getId(), target.getEmail(), e);
            target.markFailed(e.getMessage());
        }
    }
}
