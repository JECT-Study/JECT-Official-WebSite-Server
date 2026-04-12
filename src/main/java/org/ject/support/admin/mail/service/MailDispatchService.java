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

    /**
     * 실제 발송 전 템플릿 렌더링 결과를 미리 조회합니다.
     */
    public MailPreviewResponse preview(MailPreviewRequest request) {
        // 1. 활성 시나리오와 수신자 정보를 조회합니다.
        MailScenario scenario = findActiveScenario(request.mailScenarioId());
        Member receiver = findReceiver(request.receiverId());

        // 2. 공통 변수 입력값을 정규화하고 필수값 누락 여부를 검증합니다.
        Map<String, String> commonVariables = normalizeCommonVariables(request.commonVariables());
        mailTemplateValidator.validateRequiredCommonVariables(scenario.getCustomVariables(), commonVariables);

        // 3. 렌더링에 필요한 개인 변수(기수 등)와 최종 변수 맵을 구성합니다.
        Map<Long, String> semesterNames = loadSemesterNames(List.of(receiver));
        Map<String, Object> renderVariables = buildRenderVariables(
                receiver,
                commonVariables,
                semesterNames
        );

        // 4. 제목/본문 템플릿을 치환합니다.
        String subject = mailTemplateEngine.render(scenario.getSubjectTemplate(), renderVariables);
        String body = mailTemplateEngine.render(scenario.getBodyTemplate(), renderVariables);

        // 5. 미리보기 응답 DTO를 반환합니다.
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
        // 1. 활성 시나리오와 수신자 정보를 조회합니다.
        MailScenario scenario = findActiveScenario(request.mailScenarioId());
        Member receiver = findReceiver(request.receiverId());

        // 2. 공통 변수 입력값을 정규화하고 필수값을 검증합니다.
        Map<String, String> commonVariables = normalizeCommonVariables(request.commonVariables());
        mailTemplateValidator.validateRequiredCommonVariables(scenario.getCustomVariables(), commonVariables);

        // 3. 렌더링 변수를 구성하고 제목/본문을 생성합니다.
        Map<Long, String> semesterNames = loadSemesterNames(List.of(receiver));
        Map<String, Object> renderVariables = buildRenderVariables(
                receiver,
                commonVariables,
                semesterNames
        );

        String subject = mailTemplateEngine.render(scenario.getSubjectTemplate(), renderVariables);
        String body = mailTemplateEngine.render(scenario.getBodyTemplate(), renderVariables);

        // 4. 외부 메일 서비스를 호출해 테스트 메일을 발송합니다.
        try {
            emailSendService.sendEmail(request.toEmail(), subject, body);
        } catch (Exception e) {
            // 4-1. 외부 메일 서비스 오류는 도메인 예외로 변환합니다.
            throw new MailException(MailErrorCode.TEST_MAIL_SEND_FAILURE);
        }

        // 5. 테스트 발송 결과를 응답합니다.
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
        // 1. 활성 시나리오를 조회하고 수신자 목록 입력을 검증합니다.
        MailScenario scenario = findActiveScenario(request.mailScenarioId());
        validateReceivers(request.receiverIds());

        // 2. 공통 변수 입력값을 정규화하고 필수값 누락 여부를 검증합니다.
        Map<String, String> commonVariables = normalizeCommonVariables(request.commonVariables());
        mailTemplateValidator.validateRequiredCommonVariables(scenario.getCustomVariables(), commonVariables);

        // 3. 수신자 ID 목록을 실제 회원 정보 맵으로 변환합니다.
        Map<Long, Member> receiversById = findReceiversByIds(request.receiverIds());

        // 4. 발송 작업(Job)을 REQUESTED 상태로 생성합니다.
        MailDispatchJob dispatchJob = MailDispatchJob.builder()
                .scenario(scenario)
                .requestedByMemberId(null)
                .status(MailDispatchJobStatus.REQUESTED)
                .receiverCount(request.receiverIds().size())
                .commonVariablesJson(map2JsonSerializer.serializeAsString(commonVariables))
                .build();

        MailDispatchJob savedJob = mailDispatchJobRepository.save(dispatchJob);

        // 5. 수신자별 발송 대상(Target)을 생성해 일괄 저장합니다.
        List<MailDispatchTarget> targets = request.receiverIds().stream()
                .map(receiversById::get)
                .map(receiver -> MailDispatchTarget.pending(savedJob, receiver.getId(), receiver.getEmail()))
                .toList();

        mailDispatchTargetRepository.saveAll(targets);

        // 6. 생성된 발송 작업 정보를 응답합니다.
        return new MailDispatchResponse(
                savedJob.getId(),
                scenario.getId(),
                savedJob.getStatus().name(),
                savedJob.getReceiverCount()
        );
    }

    @Transactional
    public MailDispatchExecuteResponse executeDispatch(Long dispatchJobId) {
        // 1. 발송 작업을 조회하고 실행 가능 상태인지 검증합니다.
        MailDispatchJob dispatchJob = mailDispatchJobRepository.findById(dispatchJobId)
                .orElseThrow(() -> new MailException(MailErrorCode.DISPATCH_JOB_NOT_FOUND));

        validateExecutable(dispatchJob);

        // 2. 발송 대상을 조회하고 비어 있지 않은지 검증합니다.
        List<MailDispatchTarget> targets = mailDispatchTargetRepository.findAllByJobIdOrderByIdAsc(dispatchJobId);
        validateTargets(targets);

        // 3. 작업 상태를 PROCESSING으로 전환합니다.
        dispatchJob.markProcessing();

        // 4. 공통 변수/수신자/개인 변수 보조 데이터를 로딩합니다.
        MailScenario scenario = dispatchJob.getScenario();
        Map<String, String> commonVariables = string2MapSerializer.serializeAsMap(dispatchJob.getCommonVariablesJson());
        mailTemplateValidator.validateRequiredCommonVariables(scenario.getCustomVariables(), commonVariables);

        Map<Long, Member> membersById = findReceiversByIds(
                targets.stream()
                        .map(MailDispatchTarget::getReceiverId)
                        .filter(id -> id != null)
                        .toList()
        );
        Map<Long, String> semesterNames = loadSemesterNames(membersById.values().stream().toList());

        // 5. 발송 결과 집계를 위한 카운터를 초기화합니다.
        int successCount = 0;
        int failedCount = 0;

        // 6. 대상별로 렌더링/발송을 수행하고 성공/실패 수를 집계합니다.
        for (MailDispatchTarget target : targets) {
            Member receiver = membersById.get(target.getReceiverId());
            if (receiver == null) {
                target.markFailed("receiver not found");
                failedCount++;
                continue;
            }

            Map<String, Object> renderVariables = buildRenderVariables(
                    receiver,
                    commonVariables,
                    semesterNames
            );
            String subject = mailTemplateEngine.render(scenario.getSubjectTemplate(), renderVariables);
            String body = mailTemplateEngine.render(scenario.getBodyTemplate(), renderVariables);

            try {
                emailSendService.sendEmail(target.getEmail(), subject, body);
                target.markSent();
                successCount++;
            } catch (Exception e) {
                target.markFailed(extractFailureReason(e));
                failedCount++;
            }
        }

        // 7. 실패 건 존재 여부에 따라 작업 상태를 완료/실패로 확정합니다.
        if (failedCount > 0) {
            dispatchJob.markFailed();
        } else {
            dispatchJob.markCompleted();
        }

        // 8. 실행 결과 요약을 응답합니다.
        return new MailDispatchExecuteResponse(
                dispatchJob.getId(),
                dispatchJob.getStatus().name(),
                successCount,
                failedCount
        );
    }

    /**
     * 발송 작업 이력 목록을 조회합니다.
     */
    public List<MailDispatchHistoryResponse> getDispatchHistories() {
        // 1. 시나리오를 포함한 발송 작업 목록을 최신순으로 조회합니다.
        List<MailDispatchJob> dispatchJobs = mailDispatchJobRepository.findAllWithScenarioOrderByIdDesc();
        if (dispatchJobs.isEmpty()) {
            return List.of();
        }

        // 2. 조회된 작업들의 타겟 목록을 모아서 가져옵니다.
        List<Long> jobIds = dispatchJobs.stream()
                .map(MailDispatchJob::getId)
                .toList();
        List<MailDispatchTarget> targets = mailDispatchTargetRepository.findAllByJobIdIn(jobIds);

        // 3. 작업별 성공/실패 카운트를 집계합니다.
        Map<Long, Long> sentCountByJobId = countByJobAndStatus(targets, MailDispatchTargetStatus.SENT);
        Map<Long, Long> failedCountByJobId = countByJobAndStatus(targets, MailDispatchTargetStatus.FAILED);

        // 4. 이력 응답 DTO 목록으로 변환합니다.
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
        // 1. 작업/대상 정보를 조회합니다.
        MailDispatchJob dispatchJob = findDispatchJob(dispatchJobId);
        List<MailDispatchTarget> targets = mailDispatchTargetRepository.findAllByJobIdOrderByIdAsc(dispatchJobId);

        // 2. 상태별 대상 수를 계산합니다.
        int pendingCount = countByStatus(targets, MailDispatchTargetStatus.PENDING);
        int sentCount = countByStatus(targets, MailDispatchTargetStatus.SENT);
        int failedCount = countByStatus(targets, MailDispatchTargetStatus.FAILED);

        // 3. 상세 응답 DTO를 반환합니다.
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
        // 1. 작업 존재 여부를 먼저 검증합니다.
        findDispatchJob(dispatchJobId);

        // 2. 실패한 대상만 조회해 응답 DTO로 변환합니다.
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
        // 1. 시나리오 ID로 엔티티를 조회합니다.
        MailScenario scenario = mailScenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new MailException(MailErrorCode.SCENARIO_NOT_FOUND));

        // 2. 비활성 시나리오면 예외를 발생시킵니다.
        if (!scenario.isActive()) {
            throw new MailException(MailErrorCode.INACTIVE_SCENARIO);
        }

        // 3. 활성 시나리오를 반환합니다.
        return scenario;
    }

    private Member findReceiver(Long receiverId) {
        // 1. 수신자 ID로 회원 정보를 조회하고, 없으면 예외를 발생시킵니다.
        return memberRepository.findById(receiverId)
                .orElseThrow(() -> new MailException(MailErrorCode.RECEIVER_NOT_FOUND));
    }

    private void validateReceivers(List<Long> receiverIds) {
        // 1. 수신자 목록이 비어 있거나 null이면 예외를 발생시킵니다.
        if (receiverIds == null || receiverIds.isEmpty()) {
            throw new MailException(MailErrorCode.EMPTY_RECEIVERS);
        }
    }

    private void validateTargets(List<MailDispatchTarget> targets) {
        // 1. 발송 대상 목록이 비어 있거나 null이면 예외를 발생시킵니다.
        if (targets == null || targets.isEmpty()) {
            throw new MailException(MailErrorCode.EMPTY_RECEIVERS);
        }
    }

    private void validateExecutable(MailDispatchJob dispatchJob) {
        // 1. 발송 작업 상태가 REQUESTED가 아니면 실행을 차단합니다.
        if (dispatchJob.getStatus() != MailDispatchJobStatus.REQUESTED) {
            throw new MailException(MailErrorCode.INVALID_DISPATCH_JOB_STATUS);
        }
    }

    private MailDispatchJob findDispatchJob(Long dispatchJobId) {
        // 1. 시나리오 연관 정보를 함께 조회하고, 없으면 예외를 발생시킵니다.
        return mailDispatchJobRepository.findByIdWithScenario(dispatchJobId)
                .orElseThrow(() -> new MailException(MailErrorCode.DISPATCH_JOB_NOT_FOUND));
    }

    private Map<Long, Member> findReceiversByIds(List<Long> receiverIds) {
        // 1. 수신자 목록을 조회해 ID 기준 맵으로 변환합니다.
        Map<Long, Member> membersById = memberRepository.findAllById(receiverIds).stream()
                .collect(Collectors.toMap(Member::getId, Function.identity(), (first, second) -> first));

        // 2. 누락된 수신자 ID가 있으면 예외를 발생시킵니다.
        boolean hasMissingReceiver = receiverIds.stream().anyMatch(id -> !membersById.containsKey(id));
        if (hasMissingReceiver) {
            throw new MailException(MailErrorCode.RECEIVER_NOT_FOUND);
        }

        // 3. 검증된 수신자 맵을 반환합니다.
        return membersById;
    }

    private Map<Long, String> loadSemesterNames(List<Member> receivers) {
        // 1. 수신자 목록에서 기수 ID 집합을 추출합니다.
        Set<Long> semesterIds = receivers.stream()
                .map(Member::getSemesterId)
                .collect(Collectors.toSet());

        // 2. 기수 ID-이름 매핑을 조회해 반환합니다.
        return semesterRepository.findAllById(semesterIds).stream()
                .collect(Collectors.toMap(Semester::getId, Semester::getName));
    }

    private Map<String, Object> buildRenderVariables(Member receiver,
                                                     Map<String, String> commonVariables,
                                                     Map<Long, String> semesterNames) {
        // 1. 공통 변수를 기준으로 렌더링 변수 맵을 초기화합니다.
        Map<String, Object> renderVariables = new HashMap<>(commonVariables);

        // 2. 예약된 개인 변수를 계산해 렌더링 변수 맵에 병합합니다.
        for (ReservedMailVariable variable : ReservedMailVariable.values()) {
            renderVariables.put(variable.name(), resolvePersonalVariable(variable, receiver, semesterNames));
        }

        // 3. 최종 렌더링 변수 맵을 반환합니다.
        return renderVariables;
    }

    private String resolvePersonalVariable(ReservedMailVariable variable,
                                           Member receiver,
                                           Map<Long, String> semesterNames) {
        // 1. 예약 변수 타입에 따라 개인화 값을 계산해 반환합니다.
        return switch (variable) {
            case name -> nullSafe(receiver.getName());
            case semester -> nullSafe(semesterNames.get(receiver.getSemesterId()));
        };
    }

    private Map<String, String> normalizeCommonVariables(Map<String, String> commonVariables) {
        // 1. null 입력은 빈 Map으로, 그 외에는 방어적 복사본으로 반환합니다.
        return commonVariables == null ? Map.of() : new HashMap<>(commonVariables);
    }

    private String nullSafe(String value) {
        // 1. null 문자열은 빈 문자열로 치환합니다.
        return value == null ? "" : value;
    }

    private String extractFailureReason(Exception e) {
        // 1. 예외 메시지가 비어 있으면 기본 실패 사유를 반환합니다.
        if (e.getMessage() == null || e.getMessage().isBlank()) {
            return "unknown failure";
        }

        // 2. 예외 메시지를 그대로 실패 사유로 사용합니다.
        return e.getMessage();
    }

    private Map<Long, Long> countByJobAndStatus(List<MailDispatchTarget> targets, MailDispatchTargetStatus status) {
        // 1. 대상 목록에서 지정한 상태만 필터링합니다.
        // 2. 작업 ID 기준으로 그룹화해 개수를 집계합니다.
        return targets.stream()
                .filter(target -> target.getStatus() == status)
                .collect(Collectors.groupingBy(target -> target.getJob().getId(), Collectors.counting()));
    }

    private int countByStatus(List<MailDispatchTarget> targets, MailDispatchTargetStatus status) {
        // 1. 지정한 상태의 대상 수를 계산해 int로 반환합니다.
        return (int) targets.stream()
                .filter(target -> target.getStatus() == status)
                .count();
    }
}
