package org.ject.support.domain.apply.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.ject.support.common.security.AuthPrincipal;
import org.ject.support.domain.apply.dto.ApplyProfileRequest;
import org.ject.support.domain.apply.dto.ApplyStatusResponse;
import org.ject.support.domain.apply.dto.ApplyTemporaryRequest;
import org.ject.support.domain.apply.dto.SubmitApplicationRequest;
import org.ject.support.domain.apply.dto.TempApplicationFormResponse;
import org.ject.support.domain.member.JobFamily;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Apply", description = "지원 API")
public interface ApplyApiSpec {

    @Operation(
            summary = "가장 최근에 저장된 임시 지원서 조회",
            description = "가장 최근에 저장된 임시 지원서를 조회합니다.")
    TempApplicationFormResponse findTempApplicationForm(@AuthPrincipal Long memberId);

    @Operation(
            summary = "지원서 임시 저장",
            description = "지원서를 임시 저장합니다.")
    void saveApplicationTemporarily(@AuthPrincipal Long memberId,
                                    @RequestBody ApplyTemporaryRequest request);

    @Operation(
            summary = "지원서 초기화",
            description = "해당 지원자의 프로필과 임시 지원서를 제거합니다.")
    void deleteProfileAndTempApplicationForm(@AuthPrincipal Long memberId);

    @Operation(
            summary = "지원서 제출",
            description = "작성이 완료된 지원서를 제출합니다.")
    void submitApplication(@AuthPrincipal Long memberId,
                           @RequestParam JobFamily jobFamily,
                           @RequestBody SubmitApplicationRequest request);

    @Operation(
            summary = "지원 상태 확인",
            description = """
                    지원자의 지원서 제출 여부를 확인합니다.
                    - TEMP_SAVED: 작성 중인 지원서가 있는 경우
                    - SUBMITTED: 이미 지원서를 제출한 경우
                    - JOINED: 합격하여 팀에 합류한 경우
                    """)
    ApplyStatusResponse checkApplyStatus(@RequestParam @Email String email);

    @Operation(
            summary = "프로필 작성(저장)",
            description = "지원자의 프로필을 작성(저장)합니다."
    )
    void saveProfile(@AuthPrincipal Long memberId,
                     @RequestBody @Valid ApplyProfileRequest request);
}
