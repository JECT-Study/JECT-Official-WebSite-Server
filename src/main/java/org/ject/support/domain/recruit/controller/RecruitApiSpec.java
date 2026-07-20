package org.ject.support.domain.recruit.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.ject.support.domain.recruit.dto.ActiveRecruitmentResponses;

@Tag(name = "Recruit", description = "모집 공고 API")
public interface RecruitApiSpec {

    @Operation(
            summary = "활성 모집 공고 목록 조회",
            description = "현재 지원 가능한 모집 공고 목록을 조회합니다.")
    ActiveRecruitmentResponses findActiveRecruitments();
}
