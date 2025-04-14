package org.ject.support.domain.recruit.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.common.util.PeriodAccessible;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.dto.QuestionResponses;
import org.ject.support.domain.recruit.repository.QuestionRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;

    @Cacheable(value = "question", key = "#jobFamily")
    @PeriodAccessible
    @Transactional(readOnly = true)
    public QuestionResponses findQuestions(final JobFamily jobFamily) {
        return new QuestionResponses(questionRepository.findByJobFamilyOfActiveRecruit(LocalDateTime.now(), jobFamily));
    }
}
