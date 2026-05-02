package org.ject.support.domain.recruit.controller;

import lombok.RequiredArgsConstructor;
import org.ject.support.common.exception.GlobalErrorCode;
import org.ject.support.common.exception.GlobalException;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.dto.QuestionResponses;
import org.ject.support.domain.recruit.service.QuestionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/apply/questions")
@RequiredArgsConstructor
public class QuestionController implements QuestionApiSpec {

    private final QuestionService questionService;

    @Override
    @GetMapping
    @PreAuthorize("hasRole('ROLE_APPLY')")
    public QuestionResponses findQuestions(@RequestParam(required = false) JobFamily jobFamily,
                                           @RequestParam(required = false) Long recruitId) {
        if (recruitId != null) {
            return questionService.findQuestions(recruitId, jobFamily);
        }
        if (jobFamily == null) {
            throw new GlobalException(GlobalErrorCode.MISS_REQUIRED_REQUEST_PARAMETER);
        }

        return questionService.findQuestions(jobFamily);
    }
}
