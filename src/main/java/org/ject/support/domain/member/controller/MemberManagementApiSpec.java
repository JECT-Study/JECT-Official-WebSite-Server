package org.ject.support.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.dto.MemberBulkDeleteRequest;
import org.ject.support.domain.member.dto.MemberDetailResponse;
import org.ject.support.domain.member.dto.MemberRegisterRequest;
import org.ject.support.domain.member.dto.MemberResponse;
import org.ject.support.domain.member.dto.MemberUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Member", description = "구성원 관리 API")
public interface MemberManagementApiSpec {

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
    void updateMember(@PathVariable final Long memberId,
                      @RequestBody @Valid final MemberUpdateRequest request);

    @Operation(
            summary = "구성원 추가",
            description = "기입한 정보로 구성원을 추가합니다.")
    void registerMember(@RequestBody @Valid final MemberRegisterRequest request);

    @Operation(
            summary = "구성원 삭제",
            description = "선택한 구성원을 삭제합니다.")
    void deleteMember(@PathVariable final Long memberId);

    @Operation(
        summary = "구성원 다수 삭제",
        description = "선택한 다수의 구성원을 삭제합니다.")
    void deleteMembers(@RequestBody @Valid final MemberBulkDeleteRequest request);
}
