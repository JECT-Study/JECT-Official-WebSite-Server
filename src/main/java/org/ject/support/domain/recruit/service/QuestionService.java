package org.ject.support.domain.recruit.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.common.util.PeriodAccessible;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.dto.QuestionResponse;
import org.ject.support.domain.recruit.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;

    @PeriodAccessible
    @Transactional(readOnly = true)
    public List<QuestionResponse> findQuestions(final JobFamily jobFamily) {
        return questionRepository.findByJobFamilyOfActiveRecruit(LocalDateTime.now(), jobFamily);
    }
}
