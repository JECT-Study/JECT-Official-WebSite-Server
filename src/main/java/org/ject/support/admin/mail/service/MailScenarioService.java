package org.ject.support.admin.mail.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.ject.support.admin.mail.domain.MailScenario;
import org.ject.support.admin.mail.domain.MailScenarioRepository;
import org.ject.support.admin.mail.domain.MailVariable;
import org.ject.support.admin.mail.dto.MailScenarioRequest;
import org.ject.support.admin.mail.dto.MailScenarioResponse;
import org.ject.support.admin.mail.dto.MailScenarioVariableResponse;
import org.ject.support.admin.mail.exception.MailErrorCode;
import org.ject.support.admin.mail.exception.MailException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 메일 시나리오 조회/관리와 템플릿 검증을 담당하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MailScenarioService {

    private static final String SCENARIO_CODE_UNIQUE_CONSTRAINT = "uk_mail_scenario_scenario_code";

    private final MailScenarioRepository mailScenarioRepository;
    private final MailTemplateEngine mailTemplateEngine;
    private final MailTemplateValidator mailTemplateValidator;

    public List<MailScenarioResponse> getScenarios() {
        return mailScenarioRepository.findAll().stream()
                .map(MailScenarioResponse::from)
                .toList();
    }

    public MailScenarioVariableResponse getScenarioVariables(Long scenarioId) {
        // 1. 시나리오와 변수 집합을 조회합니다.
        MailScenario scenario = findScenarioById(scenarioId);
        Set<MailVariable> variables = scenario.getVariables();

        // 2. 공통 변수만 추출해 응답 형태로 변환합니다.
        List<MailScenarioVariableResponse.VariableResponse> commonVariables = variables.stream()
                .filter(MailVariable::isCommon)
                .map(v -> new MailScenarioVariableResponse.VariableResponse(v.name(), v.getLabel()))
                .toList();

        // 3. 개인 변수만 추출해 응답 형태로 변환합니다.
        List<MailScenarioVariableResponse.VariableResponse> personalVariables = variables.stream()
                .filter(v -> !v.isCommon())
                .map(v -> new MailScenarioVariableResponse.VariableResponse(v.name(), v.getLabel()))
                .toList();

        // 4. 최종 변수 목록 응답을 반환합니다.
        return new MailScenarioVariableResponse(
                scenario.getId(),
                scenario.getName(),
                commonVariables,
                personalVariables
        );
    }

    @Transactional
    public MailScenarioResponse createScenario(MailScenarioRequest request) {
        // 1. 생성 전 시나리오 코드 중복과 템플릿 유효성을 검증합니다.
        validateScenarioCodeUniquenessForCreate(request.scenarioCode());
        validateTemplates(request.variables(), request.subjectTemplate(), request.bodyTemplate());

        // 2. 요청값으로 시나리오 엔티티를 생성합니다.
        MailScenario scenario = MailScenario.builder()
                .name(request.name())
                .category(request.category())
                .scenarioCode(request.scenarioCode())
                .subjectTemplate(request.subjectTemplate())
                .bodyTemplate(request.bodyTemplate())
                .active(request.active())
                .variables(request.variables())
                .build();

        try {
            // 3. flush 시점에 발생하는 unique 제약 위반을 서비스 계층에서 도메인 예외로 변환합니다.
            return MailScenarioResponse.from(mailScenarioRepository.saveAndFlush(scenario));
        } catch (DataIntegrityViolationException e) {
            throw mapDuplicateScenarioCodeException(e);
        }
    }

    @Transactional
    public MailScenarioResponse updateScenario(Long scenarioId, MailScenarioRequest request) {
        MailScenario scenario = findScenarioById(scenarioId);
        // 2. 자기 자신을 제외한 시나리오 코드 중복과 템플릿 유효성을 검증합니다.
        validateScenarioCodeUniquenessForUpdate(scenarioId, request.scenarioCode());
        validateTemplates(request.variables(), request.subjectTemplate(), request.bodyTemplate());

        // 3. 도메인 메서드로 시나리오 값을 갱신합니다.
        scenario.update(
                request.name(),
                request.category(),
                request.scenarioCode(),
                request.subjectTemplate(),
                request.bodyTemplate(),
                request.active(),
                request.variables()
        );

        try {
            // 4. flush 시점에 unique 제약 위반이 나면 도메인 예외로 변환합니다.
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

    /**
     * 시나리오 본문 템플릿을 변수로 렌더링합니다.
     */
    @Transactional(readOnly = true)
    public String renderScenario(Long scenarioId, Map<String, Object> variables) {
        // 1. 시나리오를 조회합니다.
        MailScenario scenario = findScenarioById(scenarioId);
        // 2. 필수 공통 변수가 모두 포함됐는지 검증합니다.
        mailTemplateValidator.validateRequiredCommonVariables(scenario.getVariables(), variables);
        // 3. 본문 템플릿을 렌더링해 반환합니다.
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

    private void validateTemplates(Set<MailVariable> allowedVariables, String subjectTemplate, String bodyTemplate) {
        // 1. 제목/본문 템플릿의 기본 문법을 검증합니다.
        mailTemplateValidator.validateSyntax(subjectTemplate);
        mailTemplateValidator.validateSyntax(bodyTemplate);

        // 2. 허용된 변수만 사용했는지 검증합니다.
        mailTemplateValidator.validateAllowedPlaceholders(subjectTemplate, allowedVariables);
        mailTemplateValidator.validateAllowedPlaceholders(bodyTemplate, allowedVariables);
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
