package org.ject.support.domain.admin.service;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.ject.support.common.data.PageResponse;
import org.ject.support.common.util.String2MapSerializer;
import org.ject.support.domain.admin.dto.SubmittedApplyCountResponse;
import org.ject.support.domain.admin.dto.SubmittedApplyResponse;
import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.Apply.Status;
import org.ject.support.domain.apply.dto.ApplyPortfolioDto;
import org.ject.support.domain.apply.exception.ApplyErrorCode;
import org.ject.support.domain.apply.exception.ApplyException;
import org.ject.support.domain.apply.repository.ApplyRepository;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubmittedApplyService {

    private final ApplyRepository applyRepository;
    private final String2MapSerializer string2MapSerializer;

    @Transactional(readOnly = true)
    public Page<SubmittedApplyResponse> findSubmittedApplies(final JobFamily jobFamily,
                                                             final Pageable pageable) {
        Page<Apply> applyPage = applyRepository.findSubmittedApplies(jobFamily, pageable);

        List<SubmittedApplyResponse> content = applyPage.getContent().stream()
                .map(this::toSubmittedApplyResponse)
                .toList();

        return PageResponse.from(content, pageable, applyPage.getTotalElements());
    }

    private SubmittedApplyResponse toSubmittedApplyResponse(final Apply apply) {
        ApplicationForm submittedApplicationForm = apply.getApplicationForm();
        Map<String, String> content = string2MapSerializer.serializeAsMap(submittedApplicationForm.getContent());
        List<ApplyPortfolioDto> portfolios = submittedApplicationForm.getPortfolios()
                .stream()
                .map(ApplyPortfolioDto::from)
                .toList();

        return SubmittedApplyResponse.from(apply, content, portfolios);
    }

    public SubmittedApplyCountResponse countSubmittedApply() {
        Long count = applyRepository.countByStatus(Status.SUBMITTED);
        return new SubmittedApplyCountResponse(count);
    }

    @Transactional
    public void deleteSubmittedApply(final Long applyId) {
        Apply apply = applyRepository.findByIdAndStatusWithMember(applyId, Status.SUBMITTED)
                .orElseThrow(() -> new ApplyException(ApplyErrorCode.NOT_FOUND_APPLY));

        deleteProfileAndApplicationForm(apply);
    }

    @Transactional
    public void deleteSubmittedApplies(final List<Long> applyIds) {
        final List<Long> distinctIds = applyIds.stream().distinct().toList();
        final List<Apply> applies = applyRepository.findAllByIdAndStatusWithMember(distinctIds, Status.SUBMITTED);

        if (applies.size() != distinctIds.size()) {
            throw new ApplyException(ApplyErrorCode.NOT_FOUND_APPLY);
        }

        applies.forEach(this::deleteProfileAndApplicationForm);
    }

    private void ensureSubmitted(final Apply apply) {
        if (apply.isNotSubmitted()) {
            throw new ApplyException(ApplyErrorCode.NOT_FOUND_SUBMITTED_APPLICATION_FORM);
        }
    }

    private void deleteProfileAndApplicationForm(final Apply apply) {
        // 제출된 지원서인지 검증
        ensureSubmitted(apply);

        //  제출된 지원서 제거
        apply.deleteApplicationForm();

        // 프로필 제거
        Member applicant = apply.getMember();
        applicant.deleteProfile();
    }
}
