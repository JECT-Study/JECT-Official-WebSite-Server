package org.ject.support.admin.mail.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.common.util.Map2JsonSerializer;
import org.ject.support.common.util.String2MapSerializer;
import org.ject.support.admin.mail.domain.MailDispatchJob;
import org.ject.support.admin.mail.domain.MailDispatchJobRepository;
import org.ject.support.admin.mail.domain.MailDispatchJobStatus;
import org.ject.support.admin.mail.domain.MailDispatchTarget;
import org.ject.support.admin.mail.domain.MailDispatchTargetRepository;
import org.ject.support.admin.mail.domain.MailDispatchTargetStatus;
import org.ject.support.admin.mail.domain.MailScenario;
import org.ject.support.admin.mail.domain.MailScenarioRepository;
import org.ject.support.admin.mail.domain.ReservedMailVariable;
import org.ject.support.admin.mail.dto.MailDispatchDetailResponse;
import org.ject.support.admin.mail.dto.MailDispatchRequest;
import org.ject.support.admin.mail.dto.MailDispatchExecuteResponse;
import org.ject.support.admin.mail.dto.MailDispatchFailedTargetResponse;
import org.ject.support.admin.mail.dto.MailDispatchHistoryResponse;
import org.ject.support.admin.mail.dto.MailDispatchResponse;
import org.ject.support.admin.mail.dto.MailPreviewRequest;
import org.ject.support.admin.mail.dto.MailPreviewResponse;
import org.ject.support.admin.mail.dto.MailTestSendRequest;
import org.ject.support.admin.mail.dto.MailTestSendResponse;
import org.ject.support.admin.mail.exception.MailErrorCode;
import org.ject.support.admin.mail.exception.MailException;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.repository.MemberRepository;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.repository.SemesterRepository;
import org.ject.support.external.email.service.EmailSendService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 메일 미리보기/테스트 발송/발송 작업 생성 및 실행을 담당하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MailDispatchService {

    private final MailScenarioRepository mailScenarioRepository;
    private final MemberRepository memberRepository;
    private final SemesterRepository semesterRepository;
    private final MailDispatchJobRepository mailDispatchJobRepository;
    private final MailDispatchTargetRepository mailDispatchTargetRepository;
    private final MailTemplateEngine mailTemplateEngine;
    private final MailTemplateValidator mailTemplateValidator;
    private final Map2JsonSerializer map2JsonSerializer;
    private final String2MapSerializer string2MapSerializer;
    private final EmailSendService emailSendService;
    private final MailDispatchAsyncExecutor mailDispatchAsyncExecutor;

    /**
     * 실제 발송 전 템플릿 렌더링 결과를 미리 조회합니다.
     */
    public MailPreviewResponse preview(MailPreviewRequest request) {
        MailScenario scenario = findActiveScenario(request.mailScenarioId());
        Member receiver = findReceiver(request.receiverId());

        Map<String, String> commonVariables = normalizeCommonVariables(request.commonVariables());
        mailTemplateValidator.validateRequiredCommonVariables(scenario.getCustomVariables(), commonVariables);

        Map<Long, String> semesterNames = loadSemesterNames(List.of(receiver));
        Map<String, Object> renderVariables = buildRenderVariables(
                receiver,
                commonVariables,
                semesterNames
        );

        String subject = mailTemplateEngine.render(scenario.getSubjectTemplate(), renderVariables);
        String body = mailTemplateEngine.render(scenario.getBodyTemplate(), renderVariables);

        return new MailPreviewResponse(
                scenario.getId(),
                receiver.getId(),
                receiver.getEmail(),
                subject,
                body
        );
    }

    @Transactional
    public MailTestSendResponse sendTestMail(MailTestSendRequest request) {
        MailScenario scenario = findActiveScenario(request.mailScenarioId());
        Member receiver = findReceiver(request.receiverId());

        Map<String, String> commonVariables = normalizeCommonVariables(request.commonVariables());
        mailTemplateValidator.validateRequiredCommonVariables(scenario.getCustomVariables(), commonVariables);

        Map<Long, String> semesterNames = loadSemesterNames(List.of(receiver));
        Map<String, Object> renderVariables = buildRenderVariables(
                receiver,
                commonVariables,
                semesterNames
        );

        String subject = mailTemplateEngine.render(scenario.getSubjectTemplate(), renderVariables);
        String body = mailTemplateEngine.render(scenario.getBodyTemplate(), renderVariables);

        try {
            emailSendService.sendEmail(request.toEmail(), subject, body);
        } catch (Exception e) {
            throw new MailException(MailErrorCode.TEST_MAIL_SEND_FAILURE);
        }

        return new MailTestSendResponse(
                scenario.getId(),
                receiver.getId(),
                request.toEmail(),
                subject,
                "SENT"
        );
    }

    /**
     * 발송 작업(Job)과 대상(Target)을 생성합니다. 실제 발송은 executeDispatch에서 수행합니다.
     */
    @Transactional
    public MailDispatchResponse dispatch(MailDispatchRequest request) {
        MailScenario scenario = findActiveScenario(request.mailScenarioId());
        validateReceivers(request.receiverIds());

        Map<String, String> commonVariables = normalizeCommonVariables(request.commonVariables());
        mailTemplateValidator.validateRequiredCommonVariables(scenario.getCustomVariables(), commonVariables);

        Map<Long, Member> receiversById = findReceiversByIds(request.receiverIds());

        MailDispatchJob dispatchJob = MailDispatchJob.builder()
                .scenario(scenario)
                .requestedByMemberId(null)
                .status(MailDispatchJobStatus.REQUESTED)
                .receiverCount(request.receiverIds().size())
                .commonVariablesJson(map2JsonSerializer.serializeAsString(commonVariables))
                .build();

        MailDispatchJob savedJob = mailDispatchJobRepository.save(dispatchJob);

        List<MailDispatchTarget> targets = request.receiverIds().stream()
                .map(receiversById::get)
                .map(receiver -> MailDispatchTarget.pending(savedJob, receiver.getId(), receiver.getEmail()))
                .toList();

        mailDispatchTargetRepository.saveAll(targets);

        return new MailDispatchResponse(
                savedJob.getId(),
                scenario.getId(),
                savedJob.getStatus().name(),
                savedJob.getReceiverCount()
        );
    }

    /**
     * 발송 작업을 비동기적으로 실행합니다.
     */
    @Transactional
    public MailDispatchExecuteResponse executeDispatch(Long dispatchJobId) {
        MailDispatchJob dispatchJob = mailDispatchJobRepository.findByIdWithScenario(dispatchJobId)
                .orElseThrow(() -> new MailException(MailErrorCode.DISPATCH_JOB_NOT_FOUND));

        validateExecutable(dispatchJob);

        // 비동기 엑스큐터에 실행 위임
        mailDispatchAsyncExecutor.execute(dispatchJobId);

        return new MailDispatchExecuteResponse(
                dispatchJob.getId(),
                MailDispatchJobStatus.PROCESSING.name(),
                0, // 비동기 시작 단계이므로 카운트는 0으로 응답 (조회 API 필요)
                0
        );
    }

    /**
     * 발송 작업 이력 목록을 조회합니다.
     */
    public List<MailDispatchHistoryResponse> getDispatchHistories() {
        List<MailDispatchJob> dispatchJobs = mailDispatchJobRepository.findAllWithScenarioOrderByIdDesc();
        if (dispatchJobs.isEmpty()) {
            return List.of();
        }

        List<Long> jobIds = dispatchJobs.stream()
                .map(MailDispatchJob::getId)
                .toList();
        List<MailDispatchTarget> targets = mailDispatchTargetRepository.findAllByJobIdIn(jobIds);

        Map<Long, Long> sentCountByJobId = countByJobAndStatus(targets, MailDispatchTargetStatus.SENT);
        Map<Long, Long> failedCountByJobId = countByJobAndStatus(targets, MailDispatchTargetStatus.FAILED);

        return dispatchJobs.stream()
                .map(job -> new MailDispatchHistoryResponse(
                        job.getId(),
                        job.getScenario().getId(),
                        job.getScenario().getScenarioCode(),
                        job.getScenario().getName(),
                        job.getStatus().name(),
                        job.getReceiverCount(),
                        sentCountByJobId.getOrDefault(job.getId(), 0L).intValue(),
                        failedCountByJobId.getOrDefault(job.getId(), 0L).intValue(),
                        job.getCreatedAt(),
                        job.getStartedAt(),
                        job.getFinishedAt()
                ))
                .toList();
    }

    /**
     * 단건 발송 작업의 상태/카운트/공통 변수 정보를 조회합니다.
     */
    public MailDispatchDetailResponse getDispatchHistory(Long dispatchJobId) {
        MailDispatchJob dispatchJob = findDispatchJob(dispatchJobId);
        List<MailDispatchTarget> targets = mailDispatchTargetRepository.findAllByJobIdOrderByIdAsc(dispatchJobId);

        int pendingCount = countByStatus(targets, MailDispatchTargetStatus.PENDING);
        int sentCount = countByStatus(targets, MailDispatchTargetStatus.SENT);
        int failedCount = countByStatus(targets, MailDispatchTargetStatus.FAILED);

        return new MailDispatchDetailResponse(
                dispatchJob.getId(),
                dispatchJob.getScenario().getId(),
                dispatchJob.getScenario().getScenarioCode(),
                dispatchJob.getScenario().getName(),
                dispatchJob.getStatus().name(),
                dispatchJob.getReceiverCount(),
                pendingCount,
                sentCount,
                failedCount,
                string2MapSerializer.serializeAsMap(dispatchJob.getCommonVariablesJson()),
                dispatchJob.getCreatedAt(),
                dispatchJob.getStartedAt(),
                dispatchJob.getFinishedAt()
        );
    }

    /**
     * 발송 실패 대상 목록을 조회합니다.
     */
    public List<MailDispatchFailedTargetResponse> getFailedTargets(Long dispatchJobId) {
        findDispatchJob(dispatchJobId);

        return mailDispatchTargetRepository
                .findAllByJobIdAndStatusOrderByIdAsc(dispatchJobId, MailDispatchTargetStatus.FAILED)
                .stream()
                .map(target -> new MailDispatchFailedTargetResponse(
                        target.getId(),
                        target.getReceiverId(),
                        target.getEmail(),
                        target.getFailureReason(),
                        target.getUpdatedAt()
                ))
                .toList();
    }

    private MailScenario findActiveScenario(Long scenarioId) {
        MailScenario scenario = mailScenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new MailException(MailErrorCode.SCENARIO_NOT_FOUND));

        if (!scenario.isActive()) {
            throw new MailException(MailErrorCode.INACTIVE_SCENARIO);
        }

        return scenario;
    }

    private Member findReceiver(Long receiverId) {
        return memberRepository.findById(receiverId)
                .orElseThrow(() -> new MailException(MailErrorCode.RECEIVER_NOT_FOUND));
    }

    private void validateReceivers(List<Long> receiverIds) {
        if (receiverIds == null || receiverIds.isEmpty()) {
            throw new MailException(MailErrorCode.EMPTY_RECEIVERS);
        }
    }

    private void validateExecutable(MailDispatchJob dispatchJob) {
        if (dispatchJob.getStatus() != MailDispatchJobStatus.REQUESTED) {
            throw new MailException(MailErrorCode.INVALID_DISPATCH_JOB_STATUS);
        }
    }

    private MailDispatchJob findDispatchJob(Long dispatchJobId) {
        return mailDispatchJobRepository.findByIdWithScenario(dispatchJobId)
                .orElseThrow(() -> new MailException(MailErrorCode.DISPATCH_JOB_NOT_FOUND));
    }

    private Map<Long, Member> findReceiversByIds(List<Long> receiverIds) {
        Map<Long, Member> membersById = memberRepository.findAllById(receiverIds).stream()
                .collect(Collectors.toMap(Member::getId, Function.identity(), (first, second) -> first));

        boolean hasMissingReceiver = receiverIds.stream().anyMatch(id -> !membersById.containsKey(id));
        if (hasMissingReceiver) {
            throw new MailException(MailErrorCode.RECEIVER_NOT_FOUND);
        }

        return membersById;
    }

    private Map<Long, String> loadSemesterNames(List<Member> receivers) {
        Set<Long> semesterIds = receivers.stream()
                .map(Member::getSemesterId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        if (semesterIds.isEmpty()) {
            return Map.of();
        }

        return semesterRepository.findAllById(semesterIds).stream()
                .collect(Collectors.toMap(Semester::getId, Semester::getName));
    }

    private Map<String, Object> buildRenderVariables(Member receiver,
                                                     Map<String, String> commonVariables,
                                                     Map<Long, String> semesterNames) {
        Map<String, Object> renderVariables = new HashMap<>(commonVariables);

        for (ReservedMailVariable variable : ReservedMailVariable.values()) {
            renderVariables.put(variable.name(), resolvePersonalVariable(variable, receiver, semesterNames));
        }

        return renderVariables;
    }

    private String resolvePersonalVariable(ReservedMailVariable variable,
                                           Member receiver,
                                           Map<Long, String> semesterNames) {
        return switch (variable) {
            case name -> nullSafe(receiver.getName());
            case semester -> nullSafe(semesterNames.get(receiver.getSemesterId()));
        };
    }

    private Map<String, String> normalizeCommonVariables(Map<String, String> commonVariables) {
        return commonVariables == null ? Map.of() : new HashMap<>(commonVariables);
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private Map<Long, Long> countByJobAndStatus(List<MailDispatchTarget> targets, MailDispatchTargetStatus status) {
        return targets.stream()
                .filter(target -> target.getStatus() == status)
                .collect(Collectors.groupingBy(target -> target.getJob().getId(), Collectors.counting()));
    }

    private int countByStatus(List<MailDispatchTarget> targets, MailDispatchTargetStatus status) {
        return (int) targets.stream()
                .filter(target -> target.getStatus() == status)
                .count();
    }
}
