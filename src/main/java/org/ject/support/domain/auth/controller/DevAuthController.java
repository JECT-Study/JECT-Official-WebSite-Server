package org.ject.support.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import org.ject.support.common.security.CustomUserDetails;
import org.ject.support.common.security.jwt.JwtTokenProvider;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.repository.MemberRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Profile({"local", "dev"})
public class DevAuthController {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(
            summary = "access token 발급",
            description = "개발 편의를 위한 access token 발급 API"
    )
    @PostMapping("/access-token")
    public String getToken(@Valid @RequestBody EmailRequest request) {
        Member member = memberRepository.findByEmail(request.email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));
        Authentication authentication = jwtTokenProvider.createAuthenticationByMember(member);
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        return jwtTokenProvider.createAccessToken(authentication, customUserDetails.getMemberId());
    }

    public record EmailRequest(
            @Schema(description = "회원 이메일", example = "admin@ject.kr")
            @NotBlank String email
    ) {}
}
