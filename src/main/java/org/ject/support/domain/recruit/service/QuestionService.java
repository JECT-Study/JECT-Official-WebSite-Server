package org.ject.support.domain.recruit.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.common.exception.GlobalErrorCode;
import org.ject.support.common.exception.GlobalException;
import org.ject.support.common.util.PeriodAccessible;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.dto.QuestionResponses;
import org.ject.support.domain.recruit.exception.RecruitErrorCode;
import org.ject.support.domain.recruit.exception.RecruitException;
import org.ject.support.domain.recruit.repository.QuestionRepository;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final RecruitRepository recruitRepository;

    @Cacheable(value = "question", key = "#jobFamily")
    @PeriodAccessible
    @Transactional(readOnly = true)
    public QuestionResponses findQuestions(final JobFamily jobFamily) {
        if (jobFamily == null) {
            throw new GlobalException(GlobalErrorCode.MISS_REQUIRED_REQUEST_PARAMETER);
        }

        return new QuestionResponses(questionRepository.findByJobFamilyOfActiveRecruit(LocalDateTime.now(), jobFamily));
    }

    @Cacheable(value = "question", key = "'RECRUIT:' + #recruitId + ':' + (#jobFamily == null ? 'NONE' : #jobFamily.name())")
    @Transactional(readOnly = true)
    public QuestionResponses findQuestions(final Long recruitId, final JobFamily jobFamily) {
        LocalDateTime now = LocalDateTime.now();
        Recruit recruit = Optional.ofNullable(recruitRepository.findActiveRecruitById(recruitId, now))
                .orElseThrow(() -> new RecruitException(RecruitErrorCode.NOT_FOUND_RECRUIT));
        if (jobFamily != null && recruit.getJobFamily() != jobFamily) {
            throw new RecruitException(RecruitErrorCode.INVALID_RECRUIT_QUESTION_CONDITION);
        }

        return new QuestionResponses(questionRepository.findByRecruitIdOfActiveRecruit(now, recruitId));
    }
}
