package org.ject.support.domain.member.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.dto.MemberBulkDeleteRequest;
import org.ject.support.domain.member.dto.MemberDetailResponse;
import org.ject.support.domain.member.dto.MemberRegisterRequest;
import org.ject.support.domain.member.dto.MemberResponse;
import org.ject.support.domain.member.dto.MemberEditRequest;
import org.ject.support.domain.member.service.MemberService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
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
public class MemberManagementController implements MemberManagementApiSpec {

    private final MemberService memberService;

    @Override
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Page<MemberResponse> findMembers(@RequestParam final Role role,
                                            @RequestParam(required = false) final JobFamily jobFamily,
                                            @RequestParam(required = false) final Long semesterId,
                                            final Pageable pageable) {
        return memberService.findMembers(role, jobFamily, semesterId, pageable);
    }

    @Override
    @GetMapping("/{memberId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public MemberDetailResponse findMemberDetail(@PathVariable final Long memberId) {
        return memberService.findMemberDetail(memberId);
    }

    @Override
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void registerMember(@RequestBody @Valid final MemberRegisterRequest request) {
        memberService.registerMember(request);
    }

    @Override
    @PutMapping("/{memberId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void editMember(@PathVariable final Long memberId,
                             @RequestBody @Valid final MemberEditRequest request) {
        memberService.editMember(memberId, request);
    }

    @Override
    @DeleteMapping("/{memberId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteMember(@PathVariable final Long memberId) {
        memberService.deleteMember(memberId);
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteMembers(@RequestBody @Valid final MemberBulkDeleteRequest request) {
        memberService.deleteMembers(request.memberIds());
    }
}
