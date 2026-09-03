package org.ject.support.admin.mail.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.mail.domain.MailScenario;
import org.ject.support.admin.mail.domain.MailScenarioRepository;
import org.ject.support.admin.mail.dto.SendMailDispatchRequest;
import org.ject.support.admin.mail.exception.MailErrorCode;
import org.ject.support.admin.mail.exception.MailException;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.apply.exception.ApplyErrorCode;
import org.ject.support.domain.apply.exception.ApplyException;
import org.ject.support.domain.apply.repository.ApplyRepository;
import org.ject.support.domain.recruit.exception.RecruitErrorCode;
import org.ject.support.domain.recruit.exception.RecruitException;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MailDispatchPreparationService {

    private static final int MAX_TARGET_COUNT = 500;

    private final MailScenarioRepository mailScenarioRepository;
    private final RecruitRepository recruitRepository;
    private final ApplyRepository applyRepository;
    private final MailTemplateRenderService mailTemplateRenderService;

    public MailDispatchPlan prepare(SendMailDispatchRequest request, Long requestedByAdminId) {
        validateApplyIds(request.applyIds());
        validateRecruit(request.recruitId());
        MailScenario scenario = findActiveScenario(request.scenarioId());

        String subjectTemplate = request.subjectOverride() == null
                ? scenario.getSubjectTemplate()
                : request.subjectOverride();
        Map<String, String> inputVariables = copyInputVariables(request.inputVariables());
        mailTemplateRenderService.validate(
                scenario, subjectTemplate, scenario.getBodyTemplate(), inputVariables);

        List<Apply> applies = applyRepository.findAllByIdAndStatusWithApplicantRecruitAndForm(
                request.applyIds(), ApplyStatus.SUBMITTED);
        // 조회 결과가 요청한 모집 공고와 모두 일치하는지 검증합니다.
        validateTargets(request.recruitId(), request.applyIds(), applies);

        // 지원자별 변수를 치환한 결과를 발송 대상에 저장합니다.
        List<MailDispatchPlan.Target> targets = applies.stream()
                .map(apply -> prepareTarget(apply, subjectTemplate, scenario.getBodyTemplate(), inputVariables))
                .toList();

        return new MailDispatchPlan(
                request.scenarioId(),
                request.recruitId(),
                requestedByAdminId,
                subjectTemplate,
                scenario.getBodyTemplate(),
                inputVariables,
                targets
        );
    }

    private MailDispatchPlan.Target prepareTarget(Apply apply,
                                                  String subjectTemplate,
                                                  String bodyTemplate,
                                                  Map<String, String> inputVariables) {
        MailTemplateRenderService.RenderedMail rendered = mailTemplateRenderService.render(
                apply, subjectTemplate, bodyTemplate, inputVariables);
        String subject = rendered.subject().trim();
        long lengthWithoutWhitespace = subject.codePoints()
                .filter(character -> !Character.isWhitespace(character))
                .count();
        if (lengthWithoutWhitespace < 2 || lengthWithoutWhitespace > 40) {
            throw new MailException(MailErrorCode.INVALID_SUBJECT);
        }
        return new MailDispatchPlan.Target(
                apply.getId(), apply.getApplicant().getEmail(), subject, rendered.body());
    }

    private void validateApplyIds(List<Long> applyIds) {
        if (applyIds == null || applyIds.isEmpty() || applyIds.size() > MAX_TARGET_COUNT) {
            throw new MailException(MailErrorCode.INVALID_DISPATCH_TARGET_COUNT);
        }
        if (new HashSet<>(applyIds).size() != applyIds.size()) {
            throw new ApplyException(ApplyErrorCode.DUPLICATE_APPLY_ID);
        }
    }

    private void validateRecruit(Long recruitId) {
        if (!recruitRepository.existsById(recruitId)) {
            throw new RecruitException(RecruitErrorCode.NOT_FOUND_RECRUIT);
        }
    }

    private MailScenario findActiveScenario(Long scenarioId) {
        MailScenario scenario = mailScenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new MailException(MailErrorCode.SCENARIO_NOT_FOUND));
        if (!scenario.isActive()) {
            throw new MailException(MailErrorCode.INACTIVE_SCENARIO);
        }
        return scenario;
    }

    private void validateTargets(Long recruitId, List<Long> applyIds, List<Apply> applies) {
        if (applies.size() != applyIds.size()
                || applies.stream().anyMatch(apply -> !recruitId.equals(apply.getRecruit().getId()))) {
            throw new MailException(MailErrorCode.INVALID_DISPATCH_TARGETS);
        }
    }

    private Map<String, String> copyInputVariables(Map<String, String> inputVariables) {
        if (inputVariables == null) {
            return Map.of();
        }
        // 요청 맵을 복사해 이후 변경이 발송 계획에 영향을 주지 않도록 합니다.
        return Collections.unmodifiableMap(new LinkedHashMap<>(inputVariables));
    }
}
