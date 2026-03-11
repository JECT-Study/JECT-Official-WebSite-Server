package org.ject.support.admin.apply.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.apply.dto.AdminApplyDetailResponse;
import org.ject.support.admin.apply.dto.AdminApplyResponse;
import org.ject.support.admin.apply.dto.SubmittedApplyEditRequest;
import org.ject.support.admin.apply.repository.AdminApplyQueryRepository;
import org.ject.support.common.data.PageResponse;
import org.ject.support.common.util.Map2JsonSerializer;
import org.ject.support.common.util.String2MapSerializer;
import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.apply.domain.Portfolio;
import org.ject.support.domain.apply.dto.ApplyPortfolioDto;
import org.ject.support.domain.apply.exception.ApplyErrorCode;
import org.ject.support.domain.apply.exception.ApplyException;
import org.ject.support.domain.apply.repository.ApplyRepository;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.entity.MemberEditor;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.RecruitType;
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
    private final AdminApplyQueryRepository adminApplyQueryRepository;
    private final String2MapSerializer string2MapSerializer;
    private final Map2JsonSerializer map2JsonSerializer;

    @Transactional(readOnly = true)
    public Page<AdminApplyResponse> findApplies(final ApplyStatus applyStatus,
                                                final Long semesterId,
                                                final JobFamily jobFamily,
                                                final RecruitType recruitType,
                                                final Pageable pageable) {
        Page<Apply> applyPage = adminApplyQueryRepository.findAppliesByStatus(applyStatus, semesterId, jobFamily, recruitType, pageable);

        List<AdminApplyResponse> content = applyPage.getContent().stream()
                .map(AdminApplyResponse::from)
                .toList();

        return PageResponse.from(content, pageable, applyPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public AdminApplyDetailResponse findApply(final Long applyId,
                                              final ApplyStatus applyStatus) {
        return adminApplyQueryRepository.findApplyById(applyId, applyStatus)
                .map(this::toSubmittedApplyDetailResponse)
                .orElseThrow(() -> new ApplyException(ApplyErrorCode.NOT_FOUND_APPLY));
    }

    @Transactional
    public void updateSubmittedApply(final Long applyId,
                                     final SubmittedApplyEditRequest request) {
        Apply apply = applyRepository.findByIdAndStatusWithMember(applyId, ApplyStatus.SUBMITTED)
                .orElseThrow(() -> new ApplyException(ApplyErrorCode.NOT_FOUND_APPLY));

        Member member = apply.getMember();
        MemberEditor memberEditor = member.toEditor()
                .name(request.name())
                .phoneNumber(request.phoneNumber())
                .email(request.email())
                .jobFamily(request.jobFamily())
                .build();
        member.edit(memberEditor);

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
    public void deleteSubmittedApply(final Long applyId) {
        Apply apply = applyRepository.findByIdAndStatusWithMember(applyId, ApplyStatus.SUBMITTED)
                .orElseThrow(() -> new ApplyException(ApplyErrorCode.NOT_FOUND_APPLY));

        apply.reject();
        apply.getMember().deleteProfile();
    }

    @Transactional
    public int deleteSubmittedApplies(final List<Long> applyIds) {
        final List<Long> distinctIds = applyIds.stream().distinct().toList();
        final List<Apply> applies = applyRepository.findAllByIdAndStatusWithMember(distinctIds, ApplyStatus.SUBMITTED);

        if (applies.size() != distinctIds.size()) {
            throw new ApplyException(ApplyErrorCode.NOT_FOUND_APPLY);
        }

        applies.forEach(apply -> {
            apply.reject();
            apply.getMember().deleteProfile();
        });
        return applies.size();
    }

    private AdminApplyDetailResponse toSubmittedApplyDetailResponse(final Apply apply) {
        ApplicationForm submittedApplicationForm = apply.getApplicationForm();
        Map<String, String> content = extractContent(submittedApplicationForm);
        List<ApplyPortfolioDto> portfolios = extractPortfolios(submittedApplicationForm);
        return AdminApplyDetailResponse.from(apply);
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

    private void validateQuestions(final Map<String, String> answers, final Recruit recruit) {
        answers.keySet().stream()
                .map(Long::parseLong)
                .filter(recruit::isInvalidQuestionId)
                .findAny()
                .ifPresent(key -> {
                    throw new QuestionException(QuestionErrorCode.NOT_FOUND_QUESTION);
                });
    }
}
