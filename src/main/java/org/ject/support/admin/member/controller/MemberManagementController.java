package org.ject.support.admin.member.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.member.dto.MemberBulkDeleteRequest;
import org.ject.support.admin.member.dto.MemberDetailResponse;
import org.ject.support.admin.member.dto.MemberEditRequest;
import org.ject.support.admin.member.dto.MemberRegisterRequest;
import org.ject.support.admin.member.dto.MemberResponse;
import org.ject.support.admin.member.service.MemberManagementService;
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
public class MemberManagementController implements MemberManagementApiSpec {

    private final MemberManagementService memberManagementService;

    @Override
    @GetMapping
    public Page<MemberResponse> findMembers(@RequestParam final Role role,
                                            @RequestParam(required = false) final JobFamily jobFamily,
                                            @RequestParam(required = false) final Long semesterId,
                                            @PageableDefault(size = 15) final Pageable pageable) {
        return memberManagementService.findMembers(role, jobFamily, semesterId, pageable);
    }

    @Override
    @GetMapping("/{memberId}")
    public MemberDetailResponse findMemberDetail(@PathVariable("memberId") final Long memberId) {
        return memberManagementService.findMemberDetail(memberId);
    }

    @Override
    @PostMapping
    public void registerMember(@RequestBody @Valid final MemberRegisterRequest request) {
        memberManagementService.registerMember(request);
    }

    @Override
    @PutMapping("/{memberId}")
    public void editMember(@PathVariable("memberId") final Long memberId,
                             @RequestBody @Valid final MemberEditRequest request) {
        memberManagementService.editMember(memberId, request);
    }

    @Override
    @DeleteMapping("/{memberId}")
    public void deleteMember(@PathVariable("memberId") final Long memberId) {
        memberManagementService.deleteMember(memberId);
    }

    @Override
    @DeleteMapping
    public int deleteMembers(@RequestBody @Valid final MemberBulkDeleteRequest request) {
        return memberManagementService.deleteMembers(request.memberIds());
    }
}
