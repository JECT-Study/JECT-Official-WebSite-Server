package org.ject.support.domain.recruit.controller;

import lombok.RequiredArgsConstructor;
import org.ject.support.domain.recruit.dto.ActiveRecruitmentResponses;
import org.ject.support.domain.recruit.service.RecruitUsecase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/recruits")
@RequiredArgsConstructor
public class RecruitController implements RecruitApiSpec {

    private final RecruitUsecase recruitUsecase;

    @Override
    @GetMapping("/active")
    public ActiveRecruitmentResponses findActiveRecruitments() {
        return recruitUsecase.findActiveRecruitments();
    }
}
