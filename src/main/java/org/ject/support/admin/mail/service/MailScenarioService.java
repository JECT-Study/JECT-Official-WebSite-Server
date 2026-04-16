package org.ject.support.admin.mail.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.ject.support.admin.mail.domain.MailScenario;
import org.ject.support.admin.mail.domain.MailScenarioRepository;
import org.ject.support.admin.mail.domain.MailScenarioVariable;
import org.ject.support.admin.mail.domain.ReservedMailVariable;
import org.ject.support.admin.mail.dto.MailPreviewRequest;
import org.ject.support.admin.mail.dto.MailPreviewResponse;
import org.ject.support.admin.mail.dto.MailScenarioRequest;
import org.ject.support.admin.mail.dto.MailScenarioResponse;
import org.ject.support.admin.mail.dto.MailScenarioVariableResponse;
import org.ject.support.admin.mail.exception.MailErrorCode;
import org.ject.support.admin.mail.exception.MailException;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.repository.MemberRepository;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.repository.SemesterRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 메일 시나리오 조회/관리와 템플릿 검증/렌더링(미리보기)을 담당하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MailScenarioService {

    private static final String SCENARIO_CODE_UNIQUE_CONSTRAINT = "uk_mail_scenario_scenario_code";

    private final MailScenarioRepository mailScenarioRepository;
    private final MemberRepository memberRepository;
    private final SemesterRepository semesterRepository;
    private final MailTemplateEngine mailTemplateEngine;
    private final MailTemplateValidator mailTemplateValidator;

    public List<MailScenarioResponse> getScenarios() {
        return mailScenarioRepository.findAll().stream()
                .map(MailScenarioResponse::from)
                .toList();
    }

    public MailScenarioVariableResponse getScenarioVariables(Long scenarioId) {
        MailScenario scenario = findScenarioById(scenarioId);

        List<MailScenarioVariableResponse.CustomVariableResponse> customVariables = scenario.getCustomVariables().stream()
                .map(v -> new MailScenarioVariableResponse.CustomVariableResponse(
                        v.getKey(), v.getLabel(), v.getInputType().name(), v.isRequired(), v.getDescription()))
                .toList();

        List<String> personalVariables = Stream.of(ReservedMailVariable.values())
                .map(Enum::name)
                .toList();

        return new MailScenarioVariableResponse(
                scenario.getId(),
                scenario.getName(),
                customVariables,
                personalVariables
        );
    }

    /**
     * 메일 발송 전 수신자 정보를 바탕으로 렌더링된 결과를 미리보기합니다.
     */
    public MailPreviewResponse preview(MailPreviewRequest request) {
        MailScenario scenario = findScenarioById(request.mailScenarioId());
        Member receiver = memberRepository.findById(request.receiverId())
                .orElseThrow(() -> new MailException(MailErrorCode.RECEIVER_NOT_FOUND));

        Map<String, String> commonVariables = request.commonVariables() == null ? Map.of() : new HashMap<>(request.commonVariables());
        mailTemplateValidator.validateRequiredCommonVariables(scenario.getCustomVariables(), commonVariables);

        Map<Long, String> semesterNames = loadSemesterNames(List.of(receiver));
        Map<String, Object> renderVariables = buildRenderVariables(receiver, commonVariables, semesterNames);

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
    public MailScenarioResponse createScenario(MailScenarioRequest request) {
        Set<MailScenarioVariable> customVariables = request.customVariables().stream()
                .map(MailScenarioRequest.CustomVariableRequest::toEntity)
                .collect(Collectors.toSet());

        validateScenarioCodeUniquenessForCreate(request.scenarioCode());
        validateTemplates(customVariables, request.subjectTemplate(), request.bodyTemplate());

        MailScenario scenario = MailScenario.builder()
                .name(request.name())
                .category(request.category())
                .type(request.type())
                .scenarioCode(request.scenarioCode())
                .subjectTemplate(request.subjectTemplate())
                .bodyTemplate(request.bodyTemplate())
                .active(request.active())
                .customVariables(customVariables)
                .build();

        try {
            return MailScenarioResponse.from(mailScenarioRepository.saveAndFlush(scenario));
        } catch (DataIntegrityViolationException e) {
            throw mapDuplicateScenarioCodeException(e);
        }
    }

    @Transactional
    public MailScenarioResponse updateScenario(Long scenarioId, MailScenarioRequest request) {
        MailScenario scenario = findScenarioById(scenarioId);

        Set<MailScenarioVariable> customVariables = request.customVariables().stream()
                .map(MailScenarioRequest.CustomVariableRequest::toEntity)
                .collect(Collectors.toSet());

        validateScenarioCodeUniquenessForUpdate(scenarioId, request.scenarioCode());
        validateTemplates(customVariables, request.subjectTemplate(), request.bodyTemplate());

        scenario.update(
                request.name(),
                request.category(),
                request.type(),
                request.scenarioCode(),
                request.subjectTemplate(),
                request.bodyTemplate(),
                request.active(),
                customVariables
        );

        try {
            MailScenario savedScenario = mailScenarioRepository.saveAndFlush(scenario);
            return MailScenarioResponse.from(savedScenario);
        } catch (DataIntegrityViolationException ex) {
            throw mapDuplicateScenarioCodeException(ex);
        }
    }

    @Transactional
    public void deleteScenario(Long scenarioId) {
        MailScenario scenario = findScenarioById(scenarioId);
        mailScenarioRepository.delete(scenario);
    }

    private MailScenario findScenarioById(Long scenarioId) {
        return mailScenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new MailException(MailErrorCode.SCENARIO_NOT_FOUND));
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

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private void validateScenarioCodeUniquenessForCreate(String scenarioCode) {
        if (mailScenarioRepository.existsByScenarioCode(scenarioCode)) {
            throw new MailException(MailErrorCode.DUPLICATE_SCENARIO_CODE);
        }
    }

    private void validateScenarioCodeUniquenessForUpdate(Long scenarioId, String scenarioCode) {
        if (mailScenarioRepository.existsByScenarioCodeAndIdNot(scenarioCode, scenarioId)) {
            throw new MailException(MailErrorCode.DUPLICATE_SCENARIO_CODE);
        }
    }

    private void validateTemplates(Set<MailScenarioVariable> customVariables, String subjectTemplate, String bodyTemplate) {
        mailTemplateValidator.validateSyntax(subjectTemplate);
        mailTemplateValidator.validateSyntax(bodyTemplate);

        mailTemplateValidator.validateAllowedPlaceholders(subjectTemplate, customVariables);
        mailTemplateValidator.validateAllowedPlaceholders(bodyTemplate, customVariables);
    }

    private MailException mapDuplicateScenarioCodeException(DataIntegrityViolationException ex) {
        if (isScenarioCodeUniqueConstraintViolation(ex)) {
            return new MailException(MailErrorCode.DUPLICATE_SCENARIO_CODE);
        }

        throw ex;
    }

    private boolean isScenarioCodeUniqueConstraintViolation(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof ConstraintViolationException violationException) {
                String constraintName = violationException.getConstraintName();
                if (SCENARIO_CODE_UNIQUE_CONSTRAINT.equalsIgnoreCase(constraintName)) {
                    return true;
                }
            }
            current = current.getCause();
        }

        return false;
    }
}
