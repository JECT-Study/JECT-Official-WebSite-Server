package org.ject.support.domain.recruit.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ject.support.domain.recruit.dto.ApplyManagementRecruitSearchCondition;
import org.ject.support.domain.recruit.dto.RecruitRegisterRequest;
import org.ject.support.domain.recruit.dto.RecruitUpdateRequest;
import org.ject.support.domain.recruit.dto.ApplyManagementRecruitsResponse;
import org.ject.support.domain.recruit.service.RecruitUsecase;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/recruits")
@RequiredArgsConstructor
public class AdminRecruitController implements AdminRecruitApiSpec {

    private final RecruitUsecase recruitUsecase;

    @Override
    @PostMapping
    public void registerRecruit(@RequestBody @Valid List<RecruitRegisterRequest> requests) {
        recruitUsecase.registerRecruits(requests);
    }

    @Override
    @GetMapping("/apply-management")
    public ApplyManagementRecruitsResponse searchApplyManagementRecruits(
            @ParameterObject @ModelAttribute @Valid ApplyManagementRecruitSearchCondition condition
    ) {
        return recruitUsecase.searchApplyManagementRecruits(condition);
    }

    @Override
    @PutMapping("/{recruitId}")
    public void updateRecruit(@PathVariable Long recruitId, @RequestBody @Valid RecruitUpdateRequest request) {
        recruitUsecase.updateRecruit(recruitId, request);
    }

    @Override
    @DeleteMapping("/{recruitId}")
    public void cancelRecruit(@PathVariable Long recruitId) {
        recruitUsecase.cancelRecruit(recruitId);
    }
}
