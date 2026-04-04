package org.ject.support.admin.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.member.dto.MemberBulkDeleteRequest;
import org.ject.support.admin.member.dto.MemberDetailResponse;
import org.ject.support.admin.member.dto.MemberEditRequest;
import org.ject.support.admin.member.dto.MemberRegisterRequest;
import org.ject.support.admin.member.dto.MemberResponse;
import org.ject.support.admin.member.service.AdminMemberService;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/members")
@RequiredArgsConstructor
@Tag(name = "Member", description = "구성원 관리 API")
public class AdminMemberController {

    private final AdminMemberService adminMemberService;

    @Operation(
            summary = "구성원 목록 조회",
            description = "구성원 목록을 조회합니다. 역할, 직군, 학기 필터링이 가능합니다.")
    @GetMapping
    public Page<MemberResponse> findMembers(@RequestParam final Role role,
                                            @RequestParam(required = false) final JobFamily jobFamily,
                                            @RequestParam(required = false) final Long semesterId,
                                            @PageableDefault(size = 15) final Pageable pageable) {
        return adminMemberService.findMembers(role, jobFamily, semesterId, pageable);
    }

    @Operation(
            summary = "구성원 상세 조회",
            description = "전달한 ID에 해당하는 구성원의 상세 정보를 조회합니다")
    @GetMapping("/{memberId}")
    public MemberDetailResponse findMemberDetail(@PathVariable final Long memberId) {
        return adminMemberService.findMemberDetail(memberId);
    }

    @Operation(
            summary = "구성원 추가",
            description = "기입한 정보로 구성원을 추가합니다.")
    @PostMapping
    public void registerMember(@RequestBody @Valid final MemberRegisterRequest request) {
        adminMemberService.registerMember(request);
    }


    @Operation(
            summary = "구성원 정보 수정",
            description = "기입한 정보로 선택된 구성원을 수정합니다.")
    @PutMapping("/{memberId}")
    public void editMember(@PathVariable final Long memberId,
                           @RequestBody @Valid final MemberEditRequest request) {
        adminMemberService.editMember(memberId, request);
    }

    @Operation(
            summary = "구성원 삭제",
            description = "선택한 구성원을 삭제합니다.")
    @DeleteMapping("/{memberId}")
    public void deleteMember(@PathVariable final Long memberId) {
        adminMemberService.deleteMember(memberId);
    }

    @Operation(
            summary = "구성원 다수 삭제",
            description = "선택한 다수의 구성원을 삭제합니다.")
    @DeleteMapping
    public int deleteMembers(@RequestBody @Valid final MemberBulkDeleteRequest request) {
        return adminMemberService.deleteMembers(request.memberIds());
    }
}
