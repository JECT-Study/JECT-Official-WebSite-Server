package org.ject.support.external.n8n.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ject.support.common.util.String2MapSerializer;
import org.ject.support.domain.admin.dto.SubmittedApplyDetailResponse;
import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.dto.ApplyPortfolioDto;
import org.ject.support.domain.apply.exception.ApplyErrorCode;
import org.ject.support.domain.apply.exception.ApplyException;
import org.ject.support.domain.apply.repository.ApplyRepository;
import org.ject.support.external.n8n.client.N8nClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class N8nApplyService {

    private final ApplyRepository applyRepository;
    private final String2MapSerializer string2MapSerializer;
    private final N8nClient n8nClient;

    @Transactional(readOnly = true)
    public void sendToN8n(final Long applyId) {
        // SubmittedApplyDetailResponse n8n 전송용으로 임시 재사용
        SubmittedApplyDetailResponse response = applyRepository.findByIdAndStatusWithMember(applyId, Apply.Status.SUBMITTED)
                .map(this::toSubmittedApplyDetailResponse)
                .orElseThrow(() -> new ApplyException(ApplyErrorCode.NOT_FOUND_APPLY));
        try {
            n8nClient.send(response);
        } catch (Exception e) {
            log.error("Failed to send apply {} to n8n", applyId, e);
        }

    }

    private SubmittedApplyDetailResponse toSubmittedApplyDetailResponse(final Apply apply) {
        ApplicationForm submittedApplicationForm = apply.getApplicationForm();
        Map<String, String> content = extractContent(submittedApplicationForm);
        List<ApplyPortfolioDto> portfolios = extractPortfolios(submittedApplicationForm);
        return SubmittedApplyDetailResponse.from(apply, content, portfolios);
    }

    private Map<String, String> extractContent(final ApplicationForm applicationForm) {
        return Optional.ofNullable(applicationForm)
                .map(ApplicationForm::getContent)
                .map(string2MapSerializer::serializeAsMap)
                .orElse(Map.of());
    }

    private List<ApplyPortfolioDto> extractPortfolios(final ApplicationForm applicationForm) {
        return Optional.ofNullable(applicationForm)
                .map(ApplicationForm::getPortfolios)
                .orElse(List.of())
                .stream()
                .map(ApplyPortfolioDto::from)
                .toList();
    }
}
