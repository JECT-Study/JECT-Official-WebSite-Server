package org.ject.support.domain.recruit.service;

import lombok.RequiredArgsConstructor;
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

    @Cacheable(value = "question", key = "'RECRUIT:' + #recruitId")
    @Transactional(readOnly = true)
    public QuestionResponses findQuestions(final Long recruitId) {
        LocalDateTime now = LocalDateTime.now();
        Optional.ofNullable(recruitRepository.findActiveRecruitById(recruitId, now))
                .orElseThrow(() -> new RecruitException(RecruitErrorCode.NOT_FOUND_RECRUIT));

        return new QuestionResponses(questionRepository.findByRecruitIdOfActiveRecruit(now, recruitId));
    }
}
