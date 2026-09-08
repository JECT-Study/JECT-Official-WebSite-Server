package org.ject.support.admin.mail.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.admin.mail.domain.MailScenario;
import org.ject.support.admin.mail.domain.MailScenarioRepository;
import org.ject.support.admin.mail.dto.MailPreviewResponse;
import org.ject.support.admin.mail.dto.PreviewMailRequest;
import org.ject.support.admin.mail.exception.MailErrorCode;
import org.ject.support.admin.mail.exception.MailException;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.apply.exception.ApplyErrorCode;
import org.ject.support.domain.apply.exception.ApplyException;
import org.ject.support.domain.apply.repository.ApplyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 실제 발송 전 메일 제목과 본문 렌더링을 담당하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MailPreviewService {

    private final MailScenarioRepository mailScenarioRepository;
    private final ApplyRepository applyRepository;
    private final MailTemplateRenderService mailTemplateRenderService;

    public MailPreviewResponse preview(PreviewMailRequest request) {
        MailScenario scenario = findActiveScenario(request.scenarioId());
        Apply apply = findSubmittedApply(request.applyId());

        mailTemplateRenderService.validate(
                scenario,
                scenario.getSubjectTemplate(),
                scenario.getBodyTemplate(),
                request.inputVariables());
        MailTemplateRenderService.RenderedMail rendered = mailTemplateRenderService.render(
                apply,
                scenario.getSubjectTemplate(),
                scenario.getBodyTemplate(),
                request.inputVariables());

        return new MailPreviewResponse(
                request.scenarioId(),
                request.applyId(),
                apply.getApplicant().getEmail(),
                rendered.subject(),
                rendered.body()
        );
    }

    private MailScenario findActiveScenario(Long scenarioId) {
        MailScenario scenario = mailScenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new MailException(MailErrorCode.SCENARIO_NOT_FOUND));

        if (!scenario.isActive()) {
            throw new MailException(MailErrorCode.INACTIVE_SCENARIO);
        }

        return scenario;
    }

    private Apply findSubmittedApply(Long applyId) {
        Apply apply = applyRepository.findByIdAndStatusWithApplicant(applyId, ApplyStatus.SUBMITTED)
                .orElseThrow(() -> new ApplyException(ApplyErrorCode.NOT_FOUND_APPLY));

        if (apply.isNotSubmitted()) {
            throw new ApplyException(ApplyErrorCode.NOT_SUBMITTED);
        }

        return apply;
    }
}
