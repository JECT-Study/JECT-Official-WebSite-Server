package org.ject.support.domain.recruit.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.ject.support.domain.recruit.dto.ActiveRecruitmentResponses;
import org.ject.support.domain.recruit.dto.RecruitResponse;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Recruit", description = "모집 공고 API")
public interface RecruitApiSpec {

    @Operation(
            summary = "모집공고 단건 조회",
            description = "모집공고의 상세정보와 안내사항 및 FAQ를 조회합니다.")
    RecruitResponse getRecruit(@PathVariable Long recruitId);

    @Operation(
            summary = "활성 모집 공고 목록 조회",
            description = "현재 지원 가능한 모집 공고 목록을 조회합니다.")
    ActiveRecruitmentResponses findActiveRecruitments();
}
