package org.ject.support.domain.member.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ject.support.common.security.jwt.JwtCookieProvider;
import org.ject.support.common.security.jwt.JwtTokenProvider;
import org.ject.support.domain.member.dto.MemberDto;
import org.ject.support.domain.member.dto.MemberDto.RegisterResponse;
import org.ject.support.domain.member.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final JwtCookieProvider jwtCookieProvider;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 회원 등록 API
     * 인증번호 검증 후 발급받은 토큰을 통해 인증된 사용자만 접근 가능합니다.
     * PIN 번호를 암호화하여 임시 회원을 생성합니다.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse registerMember(@RequestHeader("Authorization") String authorizationHeader,
                                           @Valid @RequestBody MemberDto.RegisterRequest request) {

        // 인증 토큰에서 이메일 추출
        String token = authorizationHeader.substring(7); // "Bearer " 제거
        String email = jwtTokenProvider.extractEmailFromVerificationToken(token);

        // 임시 회원 생성 및 토큰 발급
        return memberService.registerTempMember(request, email);
    }
}
