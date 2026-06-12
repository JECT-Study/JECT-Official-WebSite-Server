package org.ject.support.domain.applicant.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.ject.support.common.security.AuthPrincipal;
import org.ject.support.domain.applicant.dto.ApplicantDto;
import org.ject.support.domain.applicant.dto.ApplicantProfileResponse;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Applicant", description = "지원자 API")
public interface ApplicantApiSpec {

    @Operation(
            summary = "지원자 등록",
            description = "PIN 번호를 암호화하여 지원자를 생성합니다. 인증번호 검증 후 발급받은 토큰을 통해 인증된 사용자만 접근 가능합니다.")
    boolean registerTempApplicant(HttpServletRequest request, HttpServletResponse response,
                                  @Valid @RequestBody ApplicantDto.RegisterRequest registerRequest);

    @Operation(
            summary = "지원자의 최초 정보 등록",
            description = "지원자(ROLE_APPLY)가 이름과 전화번호를 처음 등록할 때 사용합니다.")
    void registerInitialProfile(@AuthPrincipal Long applicantId,
                                @Valid @RequestBody ApplicantDto.InitialProfileRequest request);

    @Operation(
            summary = "PIN 번호 재설정",
            description = "PIN 번호를 재설정합니다.")
    void resetPin(@AuthPrincipal Long applicantId,
                  @Valid @RequestBody ApplicantDto.UpdatePinRequest request);

    @Operation(
            summary = "프로필 정보 최초 등록 여부 확인",
            description = "지원자의 최초 프로필 정보(이름, 전화번호) 등록 여부를 확인합니다.")
    boolean isInitialApplicant(@AuthPrincipal Long applicantId);

    @Operation(
            summary = "현재 로그인한 지원자 프로필 조회",
            description = "현재 로그인한 지원자의 프로필를 조회합니다.")
    ApplicantProfileResponse getCurrentApplicant(@AuthPrincipal Long applicantId);
}
