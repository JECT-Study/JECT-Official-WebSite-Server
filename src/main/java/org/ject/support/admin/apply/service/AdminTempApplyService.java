package org.ject.support.admin.apply.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.common.data.PageResponse;
import org.ject.support.common.util.String2MapSerializer;
import org.ject.support.admin.apply.dto.TempApplyDetailResponse;
import org.ject.support.admin.apply.dto.TempSavedApplyCountResponse;
import org.ject.support.admin.apply.dto.TempSavedApplyResponse;
import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.dto.ApplyPortfolioDto;
import org.ject.support.domain.apply.exception.ApplyErrorCode;
import org.ject.support.domain.apply.exception.ApplyException;
import org.ject.support.admin.apply.repository.AdminApplyQueryRepository;
import org.ject.support.domain.apply.repository.ApplyRepository;
import org.ject.support.domain.member.JobFamily;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminTempApplyService {

    private final ApplyRepository applyRepository;
    private final AdminApplyQueryRepository adminApplyQueryRepository;
    private final String2MapSerializer string2MapSerializer;

    public TempApplyDetailResponse getTempApplyDetail(Long tempApplyId) {
        Apply apply = applyRepository.findByIdAndStatusWithMember(tempApplyId, Apply.Status.TEMP_SAVED)
                .orElseThrow(() -> new ApplyException(ApplyErrorCode.NOT_FOUND_TEMP_APPLICATION_FORM));

        ApplicationForm tempApplicationForm = apply.getApplicationForm();

        return TempApplyDetailResponse.from(
                apply,
                extractContent(tempApplicationForm),
                extractPortfolios(tempApplicationForm)
        );
    }

    public TempSavedApplyCountResponse getTempSavedApplyCount() {
        Long count = applyRepository.countByStatus(Apply.Status.TEMP_SAVED);
        return new TempSavedApplyCountResponse(count);
    }

    @Transactional
    public void deleteTempApply(Long applyId) {
        Apply apply = applyRepository.findByIdAndStatusWithMember(applyId, Apply.Status.TEMP_SAVED)
                .orElseThrow(() -> new ApplyException(ApplyErrorCode.NOT_FOUND_APPLY));
        apply.getMember().deleteProfile();
        applyRepository.delete(apply);
    }

    public Page<TempSavedApplyResponse> getTempApplies(JobFamily jobFamily, Long semesterId, Pageable pageable) {
        Apply.Status tempSavedStatus = Apply.Status.TEMP_SAVED;
        Page<Apply> applyPage = adminApplyQueryRepository.findAppliesByStatus(jobFamily, tempSavedStatus, semesterId, null, pageable);

        List<TempSavedApplyResponse> content = applyPage.getContent().stream()
                .map(this::toTempSavedApplyResponse)
                .toList();

        return PageResponse.from(content, pageable, applyPage.getTotalElements());
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
