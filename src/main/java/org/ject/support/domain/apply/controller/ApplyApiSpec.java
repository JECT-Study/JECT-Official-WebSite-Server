package org.ject.support.domain.apply.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.ject.support.common.security.AuthPrincipal;
import org.ject.support.domain.apply.dto.ApplyProfileRequest;
import org.ject.support.domain.apply.dto.ApplyStatusResponse;
import org.ject.support.domain.apply.dto.ApplyTemporaryRequest;
import org.ject.support.domain.apply.dto.SubmitApplicationRequest;
import org.ject.support.domain.apply.dto.TempApplicationFormResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Apply", description = "지원 API")
public interface ApplyApiSpec {

    @Operation(
            summary = "지원서 임시 저장 조회",
            description = "지원자의 특정 공고에 대한 임시 저장 지원서를 조회합니다.")
    TempApplicationFormResponse findTempApplicationForm(@AuthPrincipal Long memberId,
                                                        @RequestParam Long recruitId);

    @Operation(
            summary = "지원서 임시 저장",
            description = "지원자의 특정 공고에 대한 지원서를 임시 저장합니다.")
    void saveApplicationTemporarily(@AuthPrincipal Long memberId,
                                    @RequestParam Long recruitId,
                                    @RequestBody ApplyTemporaryRequest request);

    @Operation(
            summary = "지원서 초기화",
            description = "지원자의 특정 공고에 대한 프로필과 임시 지원서를 제거합니다.")
    void deleteProfileAndTempApplicationForm(@AuthPrincipal Long memberId,
                                             @RequestParam Long recruitId);

    @Operation(
            summary = "지원서 제출",
            description = "지원자의 특정 공고에 대해 작성이 완료된 지원서를 제출합니다.")
    void submitApplication(@AuthPrincipal Long memberId,
                           @RequestParam Long recruitId,
                           @RequestBody SubmitApplicationRequest request);

    @Operation(
            summary = "지원 상태 확인",
            description = """
                    지원자의 특정 공고에 대한 지원서 제출 여부를 확인합니다.
                    - JOINED: 프로필을 저장한 경우
                    - TEMP_SAVED: 작성 중인 지원서가 있는 경우
                    - SUBMITTED: 이미 지원서를 제출한 경우
                    """)
    ApplyStatusResponse checkApplyStatus(@AuthPrincipal Long memberId,
                                         @RequestParam Long recruitId);

    @Operation(
            summary = "프로필 작성(저장)",
            description = "지원자의 특정 공고에 대한 프로필을 작성(저장)합니다."
    )
    void saveProfile(@AuthPrincipal Long memberId,
                     @RequestParam Long recruitId,
                     @RequestBody @Valid ApplyProfileRequest request);
}
