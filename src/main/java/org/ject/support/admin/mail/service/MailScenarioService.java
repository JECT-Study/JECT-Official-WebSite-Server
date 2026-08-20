package org.ject.support.admin.mail.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.mail.domain.MailScenario;
import org.ject.support.admin.mail.domain.MailScenarioCategory;
import org.ject.support.admin.mail.domain.MailScenarioRepository;
import org.ject.support.admin.mail.domain.MailScenarioType;
import org.ject.support.admin.mail.domain.MailScenarioVariable;
import org.ject.support.admin.mail.domain.ReservedMailVariable;
import org.ject.support.admin.mail.dto.MailScenarioRequest;
import org.ject.support.admin.mail.dto.MailScenarioResponse;
import org.ject.support.admin.mail.dto.MailScenarioVariableResponse;
import org.ject.support.admin.mail.exception.MailErrorCode;
import org.ject.support.admin.mail.exception.MailException;
import org.ject.support.common.data.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 메일 시나리오 조회/관리와 템플릿 검증을 담당하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MailScenarioService {

    private final MailScenarioRepository mailScenarioRepository;
    private final MailTemplateEngine mailTemplateEngine;
    private final MailTemplateValidator mailTemplateValidator;

    public Page<MailScenarioResponse> searchScenarios(MailScenarioCategory category,
                                                      MailScenarioType type,
                                                      Pageable pageable) {
        Page<MailScenario> scenarioPage = mailScenarioRepository.findScenarios(category, type, pageable);
        List<MailScenarioResponse> content = scenarioPage.getContent().stream()
                .map(MailScenarioResponse::from)
                .toList();
        return PageResponse.from(content, pageable, scenarioPage.getTotalElements());
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

        return MailScenarioResponse.from(mailScenarioRepository.save(scenario));
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

        return MailScenarioResponse.from(scenario);
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

    /**
     * 시나리오 본문 템플릿을 변수로 렌더링합니다.
     */
    @Transactional(readOnly = true)
    public String renderScenario(Long scenarioId, Map<String, Object> variables) {
        MailScenario scenario = findScenarioById(scenarioId);
        mailTemplateValidator.validateRequiredCommonVariables(scenario.getCustomVariables(), variables);
        return mailTemplateEngine.render(scenario.getBodyTemplate(), variables);
    }

    private void validateScenarioCodeUniquenessForCreate(String scenarioCode) {
        // 1. 동일한 시나리오 코드가 이미 존재하는지 확인합니다.
        if (mailScenarioRepository.existsByScenarioCode(scenarioCode)) {
            // 2. 중복 코드면 생성을 중단하고 예외를 발생시킵니다.
            throw new MailException(MailErrorCode.DUPLICATE_SCENARIO_CODE);
        }
    }

    private void validateScenarioCodeUniquenessForUpdate(Long scenarioId, String scenarioCode) {
        // 1. 자기 자신을 제외한 다른 시나리오와 코드 중복 여부를 확인합니다.
        if (mailScenarioRepository.existsByScenarioCodeAndIdNot(scenarioCode, scenarioId)) {
            // 2. 중복 코드면 수정을 중단하고 예외를 발생시킵니다.
            throw new MailException(MailErrorCode.DUPLICATE_SCENARIO_CODE);
        }
    }

    private void validateTemplates(Set<MailScenarioVariable> customVariables, String subjectTemplate, String bodyTemplate) {
        // 1. 제목/본문 템플릿의 기본 문법을 검증합니다.
        mailTemplateValidator.validateSyntax(subjectTemplate);
        mailTemplateValidator.validateSyntax(bodyTemplate);

        // 2. 허용된 변수만 사용했는지 검증합니다.
        mailTemplateValidator.validateAllowedPlaceholders(subjectTemplate, customVariables);
        mailTemplateValidator.validateAllowedPlaceholders(bodyTemplate, customVariables);
    }
}
