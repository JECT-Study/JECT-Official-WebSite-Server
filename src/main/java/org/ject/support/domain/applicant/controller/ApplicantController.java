package org.ject.support.domain.applicant.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ject.support.common.security.AuthPrincipal;
import org.ject.support.common.security.CustomSuccessHandler;
import org.ject.support.common.security.jwt.JwtTokenProvider;
import org.ject.support.domain.applicant.dto.ApplicantDto;
import org.ject.support.domain.applicant.dto.ApplicantProfileResponse;
import org.ject.support.domain.applicant.service.ApplicantService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/applicants")
@RequiredArgsConstructor
public class ApplicantController implements ApplicantApiSpec {

    private final ApplicantService applicantService;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomSuccessHandler customSuccessHandler;


    /**
     * 지원자 등록 API
     * 인증번호 검증 후 발급받은 토큰을 통해 인증된 사용자만 접근 가능합니다.
     * PIN 번호를 암호화하여 지원자를 생성합니다.
     */
    @Override
    @PostMapping("/apply")
    @PreAuthorize("hasRole('ROLE_VERIFICATION')")
    public boolean registerTempApplicant(HttpServletRequest request, HttpServletResponse response,
                                         @Valid @RequestBody ApplicantDto.RegisterRequest registerRequest) {

        // 쿠키에서 verification 토큰 추출
        String token = jwtTokenProvider.resolveVerificationToken(request);
        
        // 토큰에서 이메일 추출
        String email = jwtTokenProvider.extractEmailFromVerificationToken(token);

        // 지원자 생성 및 토큰 발급
        Authentication authentication = applicantService.registerTempApplicant(registerRequest, email);
        customSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        // verification 토큰은 더 이상 필요 없으므로 삭제
        jwtTokenProvider.deleteVerificationCookie(response);

        return true;
    }

    /**
     * 지원자의 최초 정보 등록 API
     * 지원자(ROLE_APPLY)가 이름과 전화번호를 처음 등록할 때 사용합니다.
     */
    @Override
    @PutMapping("/profile/initial")
    @PreAuthorize("hasRole('ROLE_APPLY')")
    public void registerInitialProfile(@AuthPrincipal Long applicantId,
                             @Valid @RequestBody ApplicantDto.InitialProfileRequest request) {

        // 지원자의 최초 프로필 정보 등록
        applicantService.registerInitialProfile(request, applicantId);
    }

    @Override
    @PutMapping("/pin")
    @PreAuthorize("hasRole('ROLE_APPLY')")
    public void resetPin(@AuthPrincipal Long applicantId,
                         @Valid @RequestBody ApplicantDto.UpdatePinRequest request) {

        // 지원자의 PIN 번호 재설정
        applicantService.updatePin(request, applicantId);
    }

    @Override
    @GetMapping("/profile/initial/status")
    @PreAuthorize("hasRole('ROLE_APPLY')")
    public boolean isInitialApplicant(@AuthPrincipal Long applicantId) {
        // 지원자의 최초 프로필 정보 등록 여부 확인
        return applicantService.checkIsInitialed(applicantId);
    }

    @Override
    @GetMapping("/me")
    @PreAuthorize("hasRole('ROLE_APPLY') or hasRole('ROLE_SEMESTER')")
    public ApplicantProfileResponse getCurrentApplicant(@AuthPrincipal Long applicantId) {
        return applicantService.getApplicantProfile(applicantId);
    }
}
