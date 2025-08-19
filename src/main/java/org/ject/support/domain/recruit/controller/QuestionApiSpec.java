package org.ject.support.domain.recruit.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.dto.QuestionResponses;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Question", description = "지원서 문항 API")
public interface QuestionApiSpec {

    @Operation(
            summary = "지원서 문항 목록 조회",
            description = "현재 모집 중인 지원서에 한해, 전달된 직군에 해당하는 문항을 모두 조회합니다.")
    QuestionResponses findQuestions(@RequestParam JobFamily jobFamily);
}
