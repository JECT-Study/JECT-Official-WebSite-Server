package org.ject.support.domain.recruit.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.ject.support.domain.recruit.dto.RecruitRegisterRequest;
import org.ject.support.domain.recruit.dto.RecruitUpdateRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Recruit", description = "모집 API")
public interface RecruitApiSpec {

    @Operation(
            summary = "모집 등록",
            description = "모집 정보를 등록합니다.")
    void registerRecruit(@RequestBody @Valid List<RecruitRegisterRequest> requests);

    @Operation(
            summary = "모집 수정",
            description = "모집 정보를 수정합니다.")
    void updateRecruit(@PathVariable Long recruitId, @RequestBody @Valid RecruitUpdateRequest request);

    @Operation(
            summary = "모집 취소",
            description = "모집 정보를 제거합니다.")
    void cancelRecruit(@PathVariable Long recruitId);
}
