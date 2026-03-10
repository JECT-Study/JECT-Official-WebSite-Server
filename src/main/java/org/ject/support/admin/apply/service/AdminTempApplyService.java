package org.ject.support.admin.apply.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.apply.dto.TempApplyDetailResponse;
import org.ject.support.admin.apply.dto.TempSavedApplyCountResponse;
import org.ject.support.admin.apply.dto.TempSavedApplyResponse;
import org.ject.support.admin.apply.repository.AdminApplyQueryRepository;
import org.ject.support.common.util.String2MapSerializer;
import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.apply.dto.ApplyPortfolioDto;
import org.ject.support.domain.apply.exception.ApplyErrorCode;
import org.ject.support.domain.apply.exception.ApplyException;
import org.ject.support.domain.apply.repository.ApplyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminTempApplyService {

    private final ApplyRepository applyRepository;
    private final AdminApplyQueryRepository adminApplyQueryRepository;
    private final String2MapSerializer string2MapSerializer;

    public TempApplyDetailResponse getTempApplyDetail(Long tempApplyId) {
        Apply apply = applyRepository.findByIdAndStatusWithMember(tempApplyId, ApplyStatus.TEMP_SAVED)
                .orElseThrow(() -> new ApplyException(ApplyErrorCode.NOT_FOUND_TEMP_APPLICATION_FORM));

        ApplicationForm tempApplicationForm = apply.getApplicationForm();

        return TempApplyDetailResponse.from(
                apply,
                extractContent(tempApplicationForm),
                extractPortfolios(tempApplicationForm)
        );
    }

    public TempSavedApplyCountResponse getTempSavedApplyCount() {
        Long count = applyRepository.countByStatus(ApplyStatus.TEMP_SAVED);
        return new TempSavedApplyCountResponse(count);
    }

    @Transactional
    public void deleteTempApply(Long applyId) {
        Apply apply = applyRepository.findByIdAndStatusWithMember(applyId, ApplyStatus.TEMP_SAVED)
                .orElseThrow(() -> new ApplyException(ApplyErrorCode.NOT_FOUND_TEMP_APPLICATION_FORM));
        apply.getMember().deleteProfile();
        applyRepository.delete(apply);
    }

    private TempSavedApplyResponse toTempSavedApplyResponse(final Apply apply) {
        ApplicationForm applicationForm = apply.getApplicationForm();
        Map<String, String> content = extractContent(applicationForm);
        List<ApplyPortfolioDto> portfolios = extractPortfolios(applicationForm);
        return TempSavedApplyResponse.from(apply, content, portfolios);
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
