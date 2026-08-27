package org.ject.support.admin.apply.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.apply.dto.AdminApplyDetailResponse;
import org.ject.support.admin.apply.dto.AdminApplyResponse;
import org.ject.support.admin.apply.dto.AdminApplySearchCondition;
import org.ject.support.admin.apply.dto.SubmittedApplyEditRequest;
import org.ject.support.admin.apply.repository.AdminApplyRepository;
import org.ject.support.common.data.PageResponse;
import org.ject.support.common.util.Map2JsonSerializer;
import org.ject.support.common.util.String2MapSerializer;
import org.ject.support.domain.applicant.entity.Applicant;
import org.ject.support.domain.applicant.entity.ApplicantEditor;
import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.apply.domain.Portfolio;
import org.ject.support.domain.apply.dto.ApplyPortfolioDto;
import org.ject.support.domain.apply.exception.ApplyErrorCode;
import org.ject.support.domain.apply.exception.ApplyException;
import org.ject.support.domain.apply.repository.ApplyRepository;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.exception.QuestionErrorCode;
import org.ject.support.domain.recruit.exception.QuestionException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminApplyService {

    private final ApplyRepository applyRepository;
    private final AdminApplyRepository adminApplyRepository;
    private final Map2JsonSerializer map2JsonSerializer;
    private final String2MapSerializer string2MapSerializer;

    @Transactional(readOnly = true)
    public Page<AdminApplyResponse> findApplies(final AdminApplySearchCondition condition,
                                                final Pageable pageable) {
        Page<Apply> applyPage = adminApplyRepository.findApplies(condition, pageable);

        List<AdminApplyResponse> content = applyPage.getContent().stream()
                .map(AdminApplyResponse::from)
                .toList();

        return PageResponse.from(content, pageable, applyPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public AdminApplyDetailResponse findApply(final Long applyId,
                                              final ApplyStatus applyStatus) {
        return adminApplyRepository.findApplyByIdByStatus(applyId, applyStatus)
                .map(apply -> AdminApplyDetailResponse.from(apply, getAnswers(apply)))
                .orElseThrow(() -> new ApplyException(ApplyErrorCode.NOT_FOUND_APPLY));
    }

    @Transactional
    public void updateSubmittedApply(final Long applyId,
                                     final SubmittedApplyEditRequest request) {
        Apply apply = adminApplyRepository.findByIdAndStatusWithApplicant(applyId, ApplyStatus.SUBMITTED)
                .orElseThrow(() -> new ApplyException(ApplyErrorCode.NOT_FOUND_APPLY));

        Applicant applicant = apply.getApplicant();
        ApplicantEditor applicantEditor = applicant.toEditor()
                .name(request.name())
                .phoneNumber(request.phoneNumber())
                .email(request.email())
                .jobFamily(request.jobFamily())
                .build();
        applicant.edit(applicantEditor);

        Map<String, String> answers = request.answers();
        validateQuestions(answers, apply.getRecruit());
        String newContent = map2JsonSerializer.serializeAsString(answers);

        List<Portfolio> newPortfolios = request.portfolios()
                .stream()
                .map(ApplyPortfolioDto::toEntity)
                .toList();

        apply.getApplicationForm()
                .updateContentAndPortfolios(newContent, newPortfolios);
    }

    @Transactional
    public void deleteApply(final Long applyId) {
        Apply apply = adminApplyRepository.findByIdWithApplicant(applyId)
                .orElseThrow(() -> new ApplyException(ApplyErrorCode.NOT_FOUND_APPLY));
        adminApplyRepository.delete(apply);
    }

    @Transactional
    public int deleteApplies(final List<Long> applyIds) {
        adminApplyRepository.deleteAllByIds(applyIds);
        return applyIds.size();
    }


    private void validateQuestions(final Map<String, String> answers, final Recruit recruit) {
        answers.keySet().stream()
                .map(Long::parseLong)
                .filter(recruit::isInvalidQuestionId)
                .findAny()
                .ifPresent(key -> {
                    throw new QuestionException(QuestionErrorCode.NOT_FOUND_QUESTION);
                });
    }

    private Map<String, String> getAnswers(final Apply apply) {
        ApplicationForm applicationForm = apply.getApplicationForm();
        if (applicationForm == null) {
            return Map.of();
        }
        return Optional.ofNullable(string2MapSerializer.serializeAsMap(applicationForm.getContent()))
                .orElse(Map.of());
    }
}
