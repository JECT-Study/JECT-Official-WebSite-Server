package org.ject.support.domain.recruit.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.ject.support.common.security.AuthPrincipal;
import org.ject.support.common.springdoc.CustomApiResponse;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.dto.ApplyTemporaryRequest;
import org.ject.support.domain.recruit.dto.ApplyTemporaryResponse;
import org.ject.support.domain.recruit.dto.SubmitApplicationRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Apply", description = "지원 API")
public interface ApplyApi {

    @Operation(
            summary = "가장 최근에 저장된 임시 지원서 조회",
            description = "가장 최근에 저장된 임시 지원서를 조회합니다.")
    @CustomApiResponse
    ApplyTemporaryResponse getTemporaryApplication(@AuthPrincipal Long memberId);

    @Operation(
            summary = "지원서 임시 저장",
            description = "지원서를 임시 저장합니다.")
    @CustomApiResponse
    void applyTemporary(@AuthPrincipal Long memberId,
                        @RequestParam JobFamily jobFamily,
                        @RequestBody ApplyTemporaryRequest request);

    @Operation(
            summary = "모든 임시 지원서 제거",
            description = "해당 지원자의 모든 임시 지원서를 제거합니다.")
    @CustomApiResponse
    void deleteTemporaryApplications(@AuthPrincipal Long memberId);

    @Operation(
            summary = "지원서 제출",
            description = "작성이 완료된 지원서를 제출합니다.")
    @CustomApiResponse
    void submitApplication(@AuthPrincipal Long memberId,
                           @RequestParam JobFamily jobFamily,
                           @RequestBody SubmitApplicationRequest request);

    @Operation(
            summary = "지원 상태 확인",
            description = "지원자의 지원서 제출 여부를 확인합니다.")
    @CustomApiResponse
    boolean checkApplyStatus(@AuthPrincipal Long memberId);
}
