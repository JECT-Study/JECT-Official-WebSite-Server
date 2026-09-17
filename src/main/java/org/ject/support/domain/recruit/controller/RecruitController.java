package org.ject.support.domain.recruit.controller;

import lombok.RequiredArgsConstructor;
import org.ject.support.domain.recruit.dto.ActiveRecruitmentResponses;
import org.ject.support.domain.recruit.dto.RecruitResponse;
import org.ject.support.domain.recruit.service.RecruitUsecase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/recruits")
@RequiredArgsConstructor
public class RecruitController implements RecruitApiSpec {

    private final RecruitUsecase recruitUsecase;

    // 모집공고 단건 조회
    @Override
    @GetMapping("/{recruitId}")
    public RecruitResponse getRecruit(@PathVariable Long recruitId) {
        return recruitUsecase.getRecruit(recruitId);
    }

    // 활성 모집공고 목록 조회
    @Override
    @GetMapping("/active")
    public ActiveRecruitmentResponses findActiveRecruitments() {
        return recruitUsecase.findActiveRecruitments();
    }
}
