package org.ject.support.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.common.security.jwt.JwtTokenProvider;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.dto.MemberDetailResponse;
import org.ject.support.domain.member.dto.MemberDto;
import org.ject.support.domain.member.dto.MemberDto.RegisterRequest;
import org.ject.support.domain.member.dto.MemberDto.UpdatePinRequest;
import org.ject.support.domain.member.dto.MemberEditRequest;
import org.ject.support.domain.member.dto.MemberRegisterRequest;
import org.ject.support.domain.member.dto.MemberResponse;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.exception.MemberErrorCode;
import org.ject.support.domain.member.exception.MemberException;
import org.ject.support.domain.member.repository.MemberRepository;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.exception.SemesterErrorCode;
import org.ject.support.domain.recruit.exception.SemesterException;
import org.ject.support.domain.recruit.repository.SemesterRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.ject.support.domain.member.exception.MemberErrorCode.ALREADY_EXIST_MEMBER;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final SemesterRepository semesterRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    /**
     * 인증된 사용자의 PIN 번호를 설정하고 임시 회원을 생성합니다.
     * 인증번호 검증 -> PIN 번호 설정 -> 임시 회원 생성 단계에서 호출됩니다.
     * 인증 토큰을 통해 검증된 이메일로 회원을 조회하고, PIN 번호를 암호화하여 저장합니다.
     */
    @Transactional
    public Authentication registerTempMember(RegisterRequest registerRequest, String email) {
        // 이메일로 회원 조회
        Member member = memberRepository.findByEmail(email)
                .orElse(null);

        if (member == null) {
            // 새로운 회원 생성
            member = createTempMemberWithPin(registerRequest, email);
        } else {
            // 기존 회원이 있는 경우 PIN 번호만 업데이트
            throw new MemberException(ALREADY_EXIST_MEMBER);
        }

        // 인증 및 토큰 발급
        return jwtTokenProvider.createAuthenticationByMember(member);
    }

    /**
     * PIN 번호가 설정된 임시 회원을 생성합니다.
     * PIN 번호는 암호화하여 저장합니다.
     */
    private Member createTempMemberWithPin(RegisterRequest registerRequest, String email) {
        String encodedPin = passwordEncoder.encode(registerRequest.pin());

        Semester semester = semesterRepository.findRecruitingSemester()
                .orElseThrow(() -> new SemesterException(SemesterErrorCode.NOT_FOUND_RECRUITING_SEMESTER));

        Member member = registerRequest.toEntity(semester.getId(), email, encodedPin);

        // TODO 이벤트 발행 후 apply 저장

        return memberRepository.save(member);
    }

    /**
     * 임시회원의 최초 프로필 정보(이름, 전화번호) 등록
     * 임시회원(ROLE_APPLY)이 최초로 이름과 전화번호를 등록할 때 호출됩니다.
     */
    @Transactional
    public void registerInitialProfile(MemberDto.InitialProfileRequest request, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));

        member.updateNameAndPhoneNumber(request.name(), request.phoneNumber());
    }

    @Transactional
    public void updatePin(UpdatePinRequest request, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));

        String encodedPin = passwordEncoder.encode(request.pin());
        member.updatePin(encodedPin);
    }

    @Transactional(readOnly = true)
    public boolean checkIsInitialed(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));

        return member.isInitialed();
    }

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
        var member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));
        var semesterId = member.getSemesterId();
        var semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_SEMESTER_OF_MEMBER));
        return MemberDetailResponse.toResponse(member, semester);
    }

    @Transactional
    public void registerMember(final MemberRegisterRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new MemberException(ALREADY_EXIST_MEMBER);
        }

        var semester = semesterRepository.findByName(request.semesterName())
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_SEMESTER_OF_MEMBER));

        var member = request.toEntity(semester);
        memberRepository.save(member);
    }

    @Transactional
    public void editMember(final Long memberId,
                           final MemberEditRequest request) {
        var member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));

        validateEmailUniqueness(request.email(), member);

        var editorBuilder = member.toEditor();

        if (request.semesterName() != null) {
            var semester = semesterRepository.findByName(request.semesterName())
                    .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_SEMESTER_OF_MEMBER));
            editorBuilder.semesterId(semester.getId());
        }

        var editor = editorBuilder
                .name(request.name())
                .phoneNumber(request.phoneNumber())
                .email(request.email())
                .jobFamily(request.jobFamily())
                .role(request.role())
                .build();

        member.edit(editor);
    }

    @Transactional
    public void deleteMember(final Long memberId) {
        var member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));
        memberRepository.delete(member);
    }

    @Transactional
    public void deleteMembers(final List<Long> memberIds) {
        var members = memberRepository.findAllById(memberIds);

        if (members.size() != memberIds.size()) {
            throw new MemberException(MemberErrorCode.NOT_FOUND_MEMBER);
        }
        memberRepository.deleteAll(members);
    }

    private void validateEmailUniqueness(String newEmail, Member currentMember) {
        if (newEmail.equals(currentMember.getEmail())) {
            return;
        }

        if (memberRepository.existsByEmail(newEmail)) {
            throw new MemberException(MemberErrorCode.DUPLICATE_EMAIL);
        }
    }
}
