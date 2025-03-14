package org.ject.support.domain.recruit.controller;

import lombok.RequiredArgsConstructor;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.dto.QuestionResponse;
import org.ject.support.domain.recruit.service.QuestionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/apply/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping
    public List<QuestionResponse> findQuestions(@RequestParam JobFamily jobFamily) {
        return questionService.findQuestions(jobFamily);
    }
}
