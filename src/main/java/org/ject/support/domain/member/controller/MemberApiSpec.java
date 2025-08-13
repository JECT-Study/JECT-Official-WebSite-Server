package org.ject.support.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.ject.support.common.security.AuthPrincipal;
import org.ject.support.domain.member.dto.MemberDto;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Member", description = "회원 API")
public interface MemberApiSpec {

    @Operation(
            summary = "회원 등록",
            description = "PIN 번호를 암호화하여 임시 회원을 생성합니다. 인증번호 검증 후 발급받은 토큰을 통해 인증된 사용자만 접근 가능합니다.")
    boolean registerMember(HttpServletRequest request, HttpServletResponse response,
                           @Valid @RequestBody MemberDto.RegisterRequest registerRequest);

    @Operation(
            summary = "임시회원의 최초 정보 등록",
            description = "임시회원(ROLE_TEMP)이 이름과 전화번호를 처음 등록할 때 사용합니다.")
    void registerInitialProfile(@AuthPrincipal Long memberId,
                                @Valid @RequestBody MemberDto.InitialProfileRequest request);

    @Operation(
            summary = "PIN 번호 재설정",
            description = "PIN 번호를 재설정합니다.")
    void resetPin(@AuthPrincipal Long memberId,
                  @Valid @RequestBody MemberDto.UpdatePinRequest request);

    @Operation(
            summary = "프로필 정보 최초 등록 여부 확인",
            description = "임시회원의 최초 프로필 정보(이름, 전화번호) 등록 여부를 확인합니다.")
    boolean isInitialMember(@AuthPrincipal Long memberId);
}
