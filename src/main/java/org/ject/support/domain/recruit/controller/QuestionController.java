package org.ject.support.domain.recruit.controller;

import lombok.RequiredArgsConstructor;
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
    public QuestionResponses findQuestions(@RequestParam JobFamily jobFamily) {
        return questionService.findQuestions(jobFamily);
    }
}
