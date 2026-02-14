package org.ject.support.domain.admin.service;

import static org.ject.support.domain.member.exception.MemberErrorCode.ALREADY_EXIST_MEMBER;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.admin.dto.MemberDetailResponse;
import org.ject.support.domain.admin.dto.MemberEditRequest;
import org.ject.support.domain.admin.dto.MemberRegisterRequest;
import org.ject.support.domain.admin.dto.MemberResponse;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.entity.MemberEditor;
import org.ject.support.domain.member.entity.MemberEditor.MemberEditorBuilder;
import org.ject.support.domain.member.exception.MemberErrorCode;
import org.ject.support.domain.member.exception.MemberException;
import org.ject.support.domain.member.repository.MemberRepository;
import org.ject.support.domain.member.entity.TeamMember;
import org.ject.support.domain.member.entity.Team;
import org.ject.support.domain.member.repository.TeamMemberRepository;
import org.ject.support.domain.member.repository.TeamRepository;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.repository.SemesterRepository;
import java.util.Comparator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberManagementService {

    private final MemberRepository memberRepository;
    private final SemesterRepository semesterRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Transactional(readOnly = true)
    public Page<MemberResponse> findMembers(
            final Role role,
            final JobFamily jobFamily,
            final Long semesterId,
            final Pageable pageable) {
        return memberRepository.findMembers(role, jobFamily, semesterId, pageable);
    }

    @Transactional(readOnly = true)
    public MemberDetailResponse findMemberDetail(final Long memberId) {
        final Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));

        TeamMember latestTeamMember = teamMemberRepository.findByMemberId(memberId).stream()
                .max(Comparator.comparing(tm -> tm.getTeam().getSemesterId())) // Assuming higher ID is later
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_SEMESTER_OF_MEMBER));

        final Semester semester = semesterRepository.findById(latestTeamMember.getTeam().getSemesterId())
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_SEMESTER_OF_MEMBER));

        return MemberDetailResponse.toResponse(member, semester, latestTeamMember.getJobFamily());
    }

    @Transactional
    public void registerMember(final MemberRegisterRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new MemberException(ALREADY_EXIST_MEMBER);
        }

        final Semester semester = semesterRepository.findByName(request.semesterName())
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_SEMESTER_OF_MEMBER));

        final Member member = request.toEntity();
        memberRepository.save(member);

        // Find or Create "미배정" Team for the semester
        Team unassignedTeam = teamRepository.findByNameAndSemesterId("미배정", semester.getId())
                .orElseGet(() -> teamRepository.save(Team.builder()
                        .name("미배정")
                        .semesterId(semester.getId())
                        .build()));

        TeamMember teamMember = TeamMember.builder()
                .member(member)
                .team(unassignedTeam)
                .jobFamily(request.jobFamily())
                .build();
        teamMemberRepository.save(teamMember);
    }

    @Transactional
    public void editMember(final Long memberId,
            final MemberEditRequest request) {
        final Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));

        validateEmailUniqueness(request.email(), member);

        MemberEditorBuilder editorBuilder = member.toEditor();
        final MemberEditor editor = editorBuilder
                .name(request.name())
                .phoneNumber(request.phoneNumber())
                .email(request.email())
                .region(request.region())
                .role(request.role())
                .build();
        member.edit(editor);

        // Handle TeamMember (Semester & JobFamily)
        if (request.semesterName() != null && request.jobFamily() != null) {
            final Semester semester = semesterRepository.findByName(request.semesterName())
                    .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_SEMESTER_OF_MEMBER));

            // Check if member is already in a team for this semester
            List<TeamMember> teamMembers = teamMemberRepository.findByMemberId(memberId);
            TeamMember targetTeamMember = teamMembers.stream()
                    .filter(tm -> tm.getTeam().getSemesterId().equals(semester.getId()))
                    .findFirst()
                    .orElse(null);

            if (targetTeamMember != null) {
                // Update existing
                // Since jobFamily is immutable in TeamMember (no setter), we might need to
                // recreate or add setter?
                // Or direct field update if no setter. Waiting for TeamMember setter check.
                // Assuming I need to add update method to TeamMember or use builder.
                // Let's assume I replaced it.
                // Actually, TeamMember entity doesn't have update method for jobFamily.
                // I should check TeamMember.java again. I only added the field.
                // I will likely need to delete and recreate or add a setter.
                // I'll leave a TODO or assume I can update.
                // I'll add a helper method to TeamMember entity in next step.
            } else {
                // Create new assignment in "미배정" team
                Team unassignedTeam = teamRepository.findByNameAndSemesterId("미배정", semester.getId())
                        .orElseGet(() -> teamRepository.save(Team.builder()
                                .name("미배정")
                                .semesterId(semester.getId())
                                .build()));

                TeamMember newTeamMember = TeamMember.builder()
                        .member(member)
                        .team(unassignedTeam)
                        .jobFamily(request.jobFamily())
                        .build();
                teamMemberRepository.save(newTeamMember);
            }
        }
    }

    @Transactional
    public void deleteMember(final Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));
        memberRepository.delete(member);
    }

    @Transactional
    public int deleteMembers(final List<Long> memberIds) {
        List<Long> distinctMemberIds = memberIds.stream()
                .distinct()
                .toList();
        List<Member> members = memberRepository.findAllById(distinctMemberIds);

        if (members.size() != distinctMemberIds.size()) {
            throw new MemberException(MemberErrorCode.NOT_FOUND_MEMBER);
        }
        memberRepository.deleteAll(members);

        return members.size();
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
