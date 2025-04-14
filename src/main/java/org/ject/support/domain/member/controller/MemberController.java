package org.ject.support.domain.member.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ject.support.common.security.AuthPrincipal;
import org.ject.support.common.security.CustomSuccessHandler;
import org.ject.support.common.security.jwt.JwtTokenProvider;
import org.ject.support.domain.member.dto.MemberDto;
import org.ject.support.domain.member.service.MemberService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomSuccessHandler customSuccessHandler;


    /**
     * 회원 등록 API
     * 인증번호 검증 후 발급받은 토큰을 통해 인증된 사용자만 접근 가능합니다.
     * PIN 번호를 암호화하여 임시 회원을 생성합니다.
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_VERIFICATION')")
    public void registerMember(HttpServletRequest request, HttpServletResponse response,
                                           @Valid @RequestBody MemberDto.RegisterRequest registerRequest) {

        // 쿠키에서 verification 토큰 추출
        String token = jwtTokenProvider.resolveVerificationToken(request);
        
        // 토큰에서 이메일 추출
        String email = jwtTokenProvider.extractEmailFromVerificationToken(token);

        // 임시 회원 생성 및 토큰 발급
        Authentication authentication = memberService.registerTempMember(registerRequest, email);
        customSuccessHandler.onAuthenticationSuccess(request, response, authentication);
    }

    /**
     * 임시회원의 최초 정보 등록 API
     * 임시회원(ROLE_TEMP)이 이름과 전화번호를 처음 등록할 때 사용합니다.
     */
    @PutMapping("/profile/initial")
    @PreAuthorize("hasRole('ROLE_TEMP')")
    public void registerInitialProfile(@AuthPrincipal Long memberId,
                             @Valid @RequestBody MemberDto.InitialProfileRequest request) {

        // 임시회원의 최초 프로필 정보 등록
        memberService.registerInitialProfile(request, memberId);
    }

    @PutMapping("/pin")
    @PreAuthorize("hasRole('ROLE_TEMP')")
    public void resetPin(@AuthPrincipal Long memberId,
                         @Valid @RequestBody MemberDto.UpdatePinRequest request) {

        // 임시회원의 PIN 번호 재설정
        memberService.updatePin(request, memberId);
    }
}
