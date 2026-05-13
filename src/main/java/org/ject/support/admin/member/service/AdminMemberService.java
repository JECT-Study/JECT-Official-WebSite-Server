package org.ject.support.admin.member.service;

import static org.ject.support.domain.member.exception.MemberErrorCode.ALREADY_EXIST_MEMBER;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.member.dto.MemberDetailResponse;
import org.ject.support.admin.member.dto.MemberEditRequest;
import org.ject.support.admin.member.dto.MemberRegisterRequest;
import org.ject.support.admin.member.dto.MemberResponse;
import org.ject.support.admin.member.repository.AdminMemberRepository;
import org.ject.support.common.data.PageResponse;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MemberType;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.dto.MemberProjection;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.entity.MemberEditor;
import org.ject.support.domain.member.entity.MemberEditor.MemberEditorBuilder;
import org.ject.support.domain.member.entity.TeamMember;
import org.ject.support.domain.member.exception.MemberErrorCode;
import org.ject.support.domain.member.exception.MemberException;
import org.ject.support.domain.member.repository.MemberRepository;
import org.ject.support.domain.member.repository.TeamMemberRepository;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.repository.SemesterRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminMemberService {

    private final MemberRepository memberRepository;
    private final AdminMemberRepository adminMemberRepository;
    private final SemesterRepository semesterRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Transactional(readOnly = true)
    public Page<MemberResponse> findMembers(
            final Role role,
            final JobFamily jobFamily,
            final Long semesterId,
            final MemberType memberType,
            final Pageable pageable
    ) {
        Page<MemberProjection> projections = adminMemberRepository.findMembers(role, jobFamily, semesterId, memberType, pageable);
        List<MemberResponse> content = projections.getContent().stream()
                .map(MemberResponse::from)
                .toList();
        return PageResponse.from(content, pageable, projections.getTotalElements());
    }

    @Transactional(readOnly = true)
    public MemberDetailResponse findMemberDetail(final Long memberId) {
        final Member member = adminMemberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));
        final Long semesterId = member.getSemesterId();
        final Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_SEMESTER_OF_MEMBER));
        return MemberDetailResponse.toResponse(member, semester);
    }

    @Transactional
    public void registerMember(final MemberRegisterRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new MemberException(ALREADY_EXIST_MEMBER);
        }

        final Semester semester = semesterRepository.findByName(request.semesterName())
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_SEMESTER_OF_MEMBER));

        final Member member = request.toEntity(semester);
        adminMemberRepository.save(member);
    }

    @Transactional
    public void editMember(final Long memberId,
                           final MemberEditRequest request) {
        final Member member = adminMemberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));

        validateEmailUniqueness(request.email(), member);

        MemberEditorBuilder editorBuilder = member.toEditor();

        if (request.semesterName() != null) {
            final Semester semester = semesterRepository.findByName(request.semesterName())
                    .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_SEMESTER_OF_MEMBER));
            editorBuilder.semesterId(semester.getId());
        }

        final MemberEditor editor = editorBuilder
                .name(request.name())
                .phoneNumber(request.phoneNumber())
                .email(request.email())
                .jobFamily(request.jobFamily())
                .region(request.region())
                .role(request.role())
                .build();

        member.edit(editor);

        // TeamMember.jobFamily 동기화 (프로젝트 조회 일관성 유지)
        if (request.jobFamily() != null) {
            syncTeamMemberJobFamily(memberId, request.jobFamily());
        }
    }

    /**
     * 멤버의 직군 변경 시 해당 멤버가 속한 모든 TeamMember의 jobFamily를 동기화합니다.
     * @deprecated 단, 향후 기수별 직군 분리가 완료되면 이 로직은 제거될 수 있습니다.
     */
    @Deprecated
    private void syncTeamMemberJobFamily(final Long memberId, final JobFamily jobFamily) {
        List<TeamMember> teamMembers = teamMemberRepository.findByMemberId(memberId);
        teamMembers.forEach(teamMember -> teamMember.updateJobFamily(jobFamily));
    }

    @Transactional
    public void deleteMember(final Long memberId) {
        Member member = adminMemberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));
        adminMemberRepository.delete(member);
    }

    @Transactional
    public int deleteMembers(final List<Long> memberIds) {
        adminMemberRepository.deleteAllByIds(memberIds);
        return memberIds.size();
    }

    private void validateEmailUniqueness(final String newEmail, final Member currentMember) {
        if (newEmail.equals(currentMember.getEmail())) {
            return;
        }

        if (memberRepository.existsByEmail(newEmail)) {
            throw new MemberException(MemberErrorCode.DUPLICATE_EMAIL);
        }
    }
}
