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
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.repository.SemesterRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberManagementService {

    private final MemberRepository memberRepository;
    private final SemesterRepository semesterRepository;

    @Transactional(readOnly = true)
    public Page<MemberResponse> findMembers(
            final Role role,
            final JobFamily jobFamily,
            final Long semesterId,
            final Pageable pageable
    ) {
        return memberRepository.findMembers(role, jobFamily, semesterId, pageable);
    }

    @Transactional(readOnly = true)
    public MemberDetailResponse findMemberDetail(final Long memberId) {
        final Member member = memberRepository.findById(memberId)
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
        memberRepository.save(member);
    }

    @Transactional
    public void editMember(final Long memberId,
                           final MemberEditRequest request) {
        final Member member = memberRepository.findById(memberId)
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
