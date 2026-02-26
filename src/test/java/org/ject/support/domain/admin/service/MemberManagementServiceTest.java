package org.ject.support.domain.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.domain.admin.dto.MemberEditRequest;
import org.ject.support.domain.admin.dto.MemberRegisterRequest;
import org.ject.support.domain.admin.dto.MemberResponse;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Region;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.entity.TeamMember;
import org.ject.support.domain.member.exception.MemberErrorCode;
import org.ject.support.domain.member.exception.MemberException;
import org.ject.support.domain.member.repository.MemberRepository;
import org.ject.support.domain.member.repository.TeamMemberRepository;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.repository.SemesterRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class MemberManagementServiceTest extends UnitTestSupport {

    @InjectMocks
    private MemberManagementService memberManagementService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private SemesterRepository semesterRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    private final String TEST_NAME = "홍길동";
    private final String TEST_EMAIL = "test@example.com";
    private final String TEST_PHONE_NUMBER = "01012345678";

    @Test
    void 회원_목록_조회_성공() {
        // given
        var role = Role.SEMESTER;
        var jobFamily = JobFamily.BE;
        var semesterId = 1L;
        var pageable = PageRequest.of(0, 15);

        var memberList = List.of(
                MemberResponse.builder()
                        .id(1L)
                        .name("회원1")
                        .phoneNumber("01012345678")
                        .email("member1@test.com")
                        .jobFamily(jobFamily)
                        .semesterName("1기")
                        .build(),
                MemberResponse.builder()
                        .id(2L)
                        .name("회원2")
                        .phoneNumber("01012345679")
                        .email("member2@test.com")
                        .jobFamily(jobFamily)
                        .semesterName("1기")
                        .build()
        );
        var expectedPage = new PageImpl<>(memberList, pageable, 2);

        given(memberRepository.findMembers(role, jobFamily, semesterId, pageable))
                .willReturn(expectedPage);

        // when
        var result = memberManagementService.findMembers(role, jobFamily, semesterId, pageable);

        // then
        assertThat(result).isEqualTo(expectedPage);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        verify(memberRepository).findMembers(role, jobFamily, semesterId, pageable);
    }

    @Test
    void 회원_목록_조회_필터_없이_성공() {
        // given
        var role = Role.ADMIN;
        var pageable = PageRequest.of(0, 15);

        var memberList = List.of(
                MemberResponse.builder()
                        .id(1L)
                        .name("관리자1")
                        .phoneNumber("01012345678")
                        .email("admin1@test.com")
                        .jobFamily(JobFamily.BE)
                        .semesterName("1기")
                        .build()
        );
        var expectedPage = new PageImpl<>(memberList, pageable, 1);

        given(memberRepository.findMembers(role, null, null, pageable))
                .willReturn(expectedPage);

        // when
        var result = memberManagementService.findMembers(role, null, null, pageable);

        // then
        assertThat(result).isEqualTo(expectedPage);
        assertThat(result.getContent()).hasSize(1);
        verify(memberRepository).findMembers(role, null, null, pageable);
    }

    @Test
    void 회원_상세_조회_성공() {
        // given
        var memberId = 1L;
        var semesterId = 1L;

        var member = Member.builder()
                .id(memberId)
                .name(TEST_NAME)
                .phoneNumber(TEST_PHONE_NUMBER)
                .email(TEST_EMAIL)
                .jobFamily(JobFamily.BE)
                .role(Role.SEMESTER)
                .semesterId(semesterId)
                .build();

        var semester = Semester.builder()
                .id(semesterId)
                .name("1기")
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(semesterRepository.findById(semesterId)).willReturn(Optional.of(semester));

        // when
        var result = memberManagementService.findMemberDetail(memberId);

        // then
        assertThat(result.id()).isEqualTo(memberId);
        assertThat(result.name()).isEqualTo(TEST_NAME);
        assertThat(result.email()).isEqualTo(TEST_EMAIL);
        assertThat(result.phoneNumber()).isEqualTo(TEST_PHONE_NUMBER);
        assertThat(result.jobFamily()).isEqualTo(JobFamily.BE);
        assertThat(result.role()).isEqualTo(Role.SEMESTER);
        assertThat(result.semesterName()).isEqualTo("1");

        verify(memberRepository).findById(memberId);
        verify(semesterRepository).findById(semesterId);
    }

    @Test
    void 회원_상세_조회_실패_존재하지_않는_회원() {
        // given
        var memberId = 999L;

        given(memberRepository.findById(memberId)).willReturn(Optional.empty());

        // expected
        assertThatThrownBy(() -> memberManagementService.findMemberDetail(memberId))
                .isInstanceOf(MemberException.class)
                .extracting(e -> ((MemberException) e).getErrorCode())
                .isEqualTo(MemberErrorCode.NOT_FOUND_MEMBER);

        verify(memberRepository).findById(memberId);
    }

    @Test
    void 회원_상세_조회_실패_존재하지_않는_기수() {
        // given
        var memberId = 1L;
        var semesterId = 999L;

        var member = Member.builder()
                .id(memberId)
                .name(TEST_NAME)
                .semesterId(semesterId)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(semesterRepository.findById(semesterId)).willReturn(Optional.empty());

        // expected
        assertThatThrownBy(() -> memberManagementService.findMemberDetail(memberId))
                .isInstanceOf(MemberException.class)
                .extracting(e -> ((MemberException) e).getErrorCode())
                .isEqualTo(MemberErrorCode.NOT_FOUND_SEMESTER_OF_MEMBER);

        verify(memberRepository).findById(memberId);
        verify(semesterRepository).findById(semesterId);
    }

    @Test
    void 관리자용_회원_등록_성공() {
        // given
        var request = new MemberRegisterRequest(
                Role.SEMESTER,
                TEST_NAME,
                TEST_PHONE_NUMBER,
                TEST_EMAIL,
                JobFamily.BE,
                Region.SEOUL,
                "1기"
        );

        var semester = Semester.builder()
                .id(1L)
                .name("1기")
                .build();

        given(memberRepository.existsByEmail(TEST_EMAIL)).willReturn(false);
        given(semesterRepository.findByName("1기")).willReturn(Optional.of(semester));
        given(memberRepository.save(any(Member.class))).willReturn(any(Member.class));

        // when
        memberManagementService.registerMember(request);

        // then
        verify(memberRepository).existsByEmail(TEST_EMAIL);
        verify(semesterRepository).findByName("1기");
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void 관리자용_회원_등록_실패_이미_존재하는_이메일() {
        // given
        var request = new MemberRegisterRequest(
                Role.SEMESTER,
                TEST_NAME,
                TEST_PHONE_NUMBER,
                TEST_EMAIL,
                JobFamily.BE,
                Region.SEOUL,
                "1"
        );

        given(memberRepository.existsByEmail(TEST_EMAIL)).willReturn(true);

        // expected
        assertThatThrownBy(() -> memberManagementService.registerMember(request))
                .isInstanceOf(MemberException.class)
                .extracting(e -> ((MemberException) e).getErrorCode())
                .isEqualTo(MemberErrorCode.ALREADY_EXIST_MEMBER);

        verify(memberRepository).existsByEmail(TEST_EMAIL);
    }

    @Test
    void 회원_정보_수정_성공() {
        // given
        var memberId = 1L;
        var request = MemberEditRequest.builder()
                .role(Role.SEMESTER)
                .name("수정된이름")
                .phoneNumber("01087654321")
                .email("updated@test.com")
                .jobFamily(JobFamily.FE)
                .semesterName("1기")
                .build();

        var member = Member.builder()
                .id(memberId)
                .name(TEST_NAME)
                .phoneNumber(TEST_PHONE_NUMBER)
                .email(TEST_EMAIL)
                .jobFamily(JobFamily.BE)
                .role(Role.SEMESTER)
                .semesterId(1L)
                .build();

        var semester = Semester.builder()
                .id(1L)
                .name("1기")
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(semesterRepository.findByName("1기")).willReturn(Optional.of(semester));
        given(teamMemberRepository.findByMemberId(memberId)).willReturn(List.of());

        // when
        memberManagementService.editMember(memberId, request);

        // then
        verify(memberRepository).findById(memberId);
        verify(teamMemberRepository).findByMemberId(memberId);
        assertThat(member.getName()).isEqualTo(request.name());
        assertThat(member.getPhoneNumber()).isEqualTo(request.phoneNumber());
        assertThat(member.getEmail()).isEqualTo(request.email());
        assertThat(member.getJobFamily()).isEqualTo(request.jobFamily());
        assertThat(member.getSemesterId()).isEqualTo(semester.getId());
    }

    @Test
    void 회원_정보_수정_시_TeamMember_jobFamily_동기화() {
        // given
        var memberId = 1L;
        var request = MemberEditRequest.builder()
                .role(Role.SEMESTER)
                .name("수정된이름")
                .phoneNumber("01087654321")
                .email("updated@test.com")
                .jobFamily(JobFamily.PM)  // BE -> PM으로 변경
                .semesterName("1기")
                .build();

        var member = Member.builder()
                .id(memberId)
                .name(TEST_NAME)
                .phoneNumber(TEST_PHONE_NUMBER)
                .email(TEST_EMAIL)
                .jobFamily(JobFamily.BE)
                .role(Role.SEMESTER)
                .semesterId(1L)
                .build();

        var semester = Semester.builder()
                .id(1L)
                .name("1기")
                .build();

        // 해당 멤버가 속한 TeamMember 목록
        var teamMember1 = TeamMember.builder()
                .id(1L)
                .member(member)
                .jobFamily(JobFamily.BE)
                .build();
        var teamMember2 = TeamMember.builder()
                .id(2L)
                .member(member)
                .jobFamily(JobFamily.BE)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(semesterRepository.findByName("1기")).willReturn(Optional.of(semester));
        given(teamMemberRepository.findByMemberId(memberId)).willReturn(List.of(teamMember1, teamMember2));

        // when
        memberManagementService.editMember(memberId, request);

        // then
        verify(teamMemberRepository).findByMemberId(memberId);
        // TeamMember의 jobFamily도 PM으로 변경되어야 함
        assertThat(teamMember1.getJobFamily()).isEqualTo(JobFamily.PM);
        assertThat(teamMember2.getJobFamily()).isEqualTo(JobFamily.PM);
    }

    @Test
    void 회원_정보_수정_실패_존재하지_않는_회원() {
        // given
        var memberId = 999L;
        var request = MemberEditRequest.builder()
                .role(Role.SEMESTER)
                .name("수정된이름")
                .phoneNumber("01087654321")
                .email("updated@test.com")
                .jobFamily(JobFamily.FE)
                .semesterName("2")
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.empty());

        // expected
        assertThatThrownBy(() -> memberManagementService.editMember(memberId, request))
                .isInstanceOf(MemberException.class)
                .extracting(e -> ((MemberException) e).getErrorCode())
                .isEqualTo(MemberErrorCode.NOT_FOUND_MEMBER);

        verify(memberRepository).findById(memberId);
    }

    @Test
    void 단일_회원_삭제_성공() {
        // given
        var memberId = 1L;
        var member = Member.builder()
                .id(memberId)
                .name(TEST_NAME)
                .email(TEST_EMAIL)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        doNothing().when(memberRepository).delete(member);

        // when
        memberManagementService.deleteMember(memberId);

        // then
        verify(memberRepository).findById(memberId);
        verify(memberRepository).delete(member);
    }

    @Test
    void 단일_회원_삭제_실패_존재하지_않는_회원() {
        // given
        var memberId = 999L;

        given(memberRepository.findById(memberId)).willReturn(Optional.empty());

        // expected
        assertThatThrownBy(() -> memberManagementService.deleteMember(memberId))
                .isInstanceOf(MemberException.class)
                .extracting(e -> ((MemberException) e).getErrorCode())
                .isEqualTo(MemberErrorCode.NOT_FOUND_MEMBER);

        verify(memberRepository).findById(memberId);
    }

    @Test
    void 다중_회원_삭제_성공() {
        // given
        var memberIds = List.of(1L, 2L, 3L);
        var members = List.of(
                Member.builder().id(1L).name("가젝트").email("member1@test.com").build(),
                Member.builder().id(2L).name("나젝트").email("member2@test.com").build(),
                Member.builder().id(3L).name("다젝트").email("member3@test.com").build()
        );

        given(memberRepository.findAllById(memberIds)).willReturn(members);
        doNothing().when(memberRepository).deleteAll(members);

        // when
        memberManagementService.deleteMembers(memberIds);

        // then
        verify(memberRepository).findAllById(memberIds);
        verify(memberRepository).deleteAll(members);
    }

    @Test
    void 다중_회원_삭제_실패_일부_회원이_존재하지_않음() {
        // given
        var memberIds = List.of(1L, 2L, 999L); // 999L은 존재하지 않는 회원
        var members = List.of(
                Member.builder().id(1L).name("가젝트").email("member1@test.com").build(),
                Member.builder().id(2L).name("나젝트").email("member2@test.com").build()
                // 999L에 해당하는 회원은 없음
        );

        given(memberRepository.findAllById(memberIds)).willReturn(members);

        // expected
        assertThatThrownBy(() -> memberManagementService.deleteMembers(memberIds))
                .isInstanceOf(MemberException.class)
                .extracting(e -> ((MemberException) e).getErrorCode())
                .isEqualTo(MemberErrorCode.NOT_FOUND_MEMBER);

        verify(memberRepository).findAllById(memberIds);
    }

    @Test
    void 다중_회원_삭제_실패_빈_목록() {
        // given
        List<Long> emptyMemberIds = List.of();
        List<Member> emptyMembers = List.of();

        given(memberRepository.findAllById(emptyMemberIds)).willReturn(emptyMembers);

        // when
        memberManagementService.deleteMembers(emptyMemberIds);

        // then
        verify(memberRepository).findAllById(emptyMemberIds);
        verify(memberRepository).deleteAll(emptyMembers);
    }
}
