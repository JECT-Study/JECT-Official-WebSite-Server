package org.ject.support.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.ject.support.common.security.AuthPrincipal;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.dto.MemberBulkDeleteRequest;
import org.ject.support.domain.member.dto.MemberDetailResponse;
import org.ject.support.domain.member.dto.MemberDto;
import org.ject.support.domain.member.dto.MemberRegisterRequest;
import org.ject.support.domain.member.dto.MemberResponse;
import org.ject.support.domain.member.dto.MemberUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Member", description = "회원 API")
public interface MemberApiSpec {

    @Operation(
            summary = "회원 등록",
            description = "PIN 번호를 암호화하여 임시 회원을 생성합니다. 인증번호 검증 후 발급받은 토큰을 통해 인증된 사용자만 접근 가능합니다.")
    boolean registerMember(HttpServletRequest request, HttpServletResponse response,
                           @Valid @RequestBody MemberDto.RegisterRequest registerRequest);

    @Operation(
            summary = "임시회원의 최초 정보 등록",
            description = "임시회원(ROLE_APPLY)이 이름과 전화번호를 처음 등록할 때 사용합니다.")
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

    @Operation(
            summary = "승인된 구성원 목록 조회",
            description = "승인된 구성원 목록을 조회합니다. 역할, 직군, 학기 필터링이 가능합니다.")
    Page<MemberResponse> findMembers(@RequestParam final Role role,
                                     @RequestParam(required = false) final JobFamily jobFamily,
                                     @RequestParam(required = false) final Long semesterId,
                                     final Pageable pageable);

    @Operation(
            summary = "승인된 구성원 상세 조회",
            description = "전달한 ID에 해당하는 구성원의 상세 정보를 조회합니다")
    MemberDetailResponse findMemberDetail(@PathVariable final Long memberId);


    @Operation(
            summary = "구성원 정보 수정",
            description = "기입한 정보로 선택된 구성원을 수정합니다.")
    void updateMember(@PathVariable final Long recruitId,
                      @RequestBody @Valid final MemberUpdateRequest request);

    @Operation(
            summary = "구성원 추가",
            description = "기입한 정보로 구성원을 추가합니다.")
    void registerMember(@RequestBody @Valid final MemberRegisterRequest request);

    @Operation(
            summary = "구성원 삭제",
            description = "선택한 구성원을 삭제합니다.")
    void deleteMember(@PathVariable final Long memberIds);

    @Operation(
        summary = "구성원 다수 삭제",
        description = "선택한 다수의 구성원을 삭제합니다.")
    void deleteMembers(@RequestBody @Valid final MemberBulkDeleteRequest request);
}
