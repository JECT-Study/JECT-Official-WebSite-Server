package org.ject.support.admin.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import org.ject.support.admin.member.dto.MemberEditRequest;
import org.ject.support.admin.member.dto.MemberRegisterRequest;
import org.ject.support.admin.member.repository.AdminMemberRepository;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Region;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.dto.MemberProjection;
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

class AdminMemberServiceTest extends UnitTestSupport {

    @InjectMocks
    private AdminMemberService adminMemberService;

    @Mock
    private AdminMemberRepository adminMemberRepository;

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
                new MemberProjection(1L, "회원1", "01012345678", "member1@test.com", jobFamily, "1기"),
                new MemberProjection(2L, "회원2", "01012345679", "member2@test.com", jobFamily, "1기")
        );
        var expectedPage = new PageImpl<>(memberList, pageable, 2);

        given(adminMemberRepository.findMembers(role, jobFamily, semesterId, pageable))
                .willReturn(expectedPage);

        // when
        var result = adminMemberService.findMembers(role, jobFamily, semesterId, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        verify(adminMemberRepository).findMembers(role, jobFamily, semesterId, pageable);
    }

    @Test
    void 회원_목록_조회_필터_없이_성공() {
        // given
        var role = Role.ADMIN;
        var pageable = PageRequest.of(0, 15);

        var memberList = List.of(
                new MemberProjection(1L, "관리자1", "01012345678", "admin1@test.com", JobFamily.BE, "1기")
        );
        var expectedPage = new PageImpl<>(memberList, pageable, 1);

        given(adminMemberRepository.findMembers(role, null, null, pageable))
                .willReturn(expectedPage);

        // when
        var result = adminMemberService.findMembers(role, null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        verify(adminMemberRepository).findMembers(role, null, null, pageable);
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

        given(adminMemberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(semesterRepository.findById(semesterId)).willReturn(Optional.of(semester));

        // when
        var result = adminMemberService.findMemberDetail(memberId);

        // then
        assertThat(result.id()).isEqualTo(memberId);
        assertThat(result.name()).isEqualTo(TEST_NAME);
        assertThat(result.email()).isEqualTo(TEST_EMAIL);
        assertThat(result.phoneNumber()).isEqualTo(TEST_PHONE_NUMBER);
        assertThat(result.jobFamily()).isEqualTo(JobFamily.BE);
        assertThat(result.role()).isEqualTo(Role.SEMESTER);
        assertThat(result.semesterName()).isEqualTo("1");

        verify(adminMemberRepository).findById(memberId);
        verify(semesterRepository).findById(semesterId);
    }

    @Test
    void 회원_상세_조회_실패_존재하지_않는_회원() {
        // given
        var memberId = 999L;

        given(adminMemberRepository.findById(memberId)).willReturn(Optional.empty());

        // expected
        assertThatThrownBy(() -> adminMemberService.findMemberDetail(memberId))
                .isInstanceOf(MemberException.class)
                .extracting(e -> ((MemberException) e).getErrorCode())
                .isEqualTo(MemberErrorCode.NOT_FOUND_MEMBER);

        verify(adminMemberRepository).findById(memberId);
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

        given(adminMemberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(semesterRepository.findById(semesterId)).willReturn(Optional.empty());

        // expected
        assertThatThrownBy(() -> adminMemberService.findMemberDetail(memberId))
                .isInstanceOf(MemberException.class)
                .extracting(e -> ((MemberException) e).getErrorCode())
                .isEqualTo(MemberErrorCode.NOT_FOUND_SEMESTER_OF_MEMBER);

        verify(adminMemberRepository).findById(memberId);
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
        given(adminMemberRepository.save(any(Member.class))).willReturn(any(Member.class));

        // when
        adminMemberService.registerMember(request);

        // then
        verify(memberRepository).existsByEmail(TEST_EMAIL);
        verify(semesterRepository).findByName("1기");
        verify(adminMemberRepository).save(any(Member.class));
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
        assertThatThrownBy(() -> adminMemberService.registerMember(request))
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

        given(adminMemberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(semesterRepository.findByName("1기")).willReturn(Optional.of(semester));
        given(teamMemberRepository.findByMemberId(memberId)).willReturn(List.of());

        // when
        adminMemberService.editMember(memberId, request);

        // then
        verify(adminMemberRepository).findById(memberId);
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

        given(adminMemberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(semesterRepository.findByName("1기")).willReturn(Optional.of(semester));
        given(teamMemberRepository.findByMemberId(memberId)).willReturn(List.of(teamMember1, teamMember2));

        // when
        adminMemberService.editMember(memberId, request);

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

        given(adminMemberRepository.findById(memberId)).willReturn(Optional.empty());

        // expected
        assertThatThrownBy(() -> adminMemberService.editMember(memberId, request))
                .isInstanceOf(MemberException.class)
                .extracting(e -> ((MemberException) e).getErrorCode())
                .isEqualTo(MemberErrorCode.NOT_FOUND_MEMBER);

        verify(adminMemberRepository).findById(memberId);
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

        given(adminMemberRepository.findById(memberId)).willReturn(Optional.of(member));
        doNothing().when(adminMemberRepository).delete(member);

        // when
        adminMemberService.deleteMember(memberId);

        // then
        verify(adminMemberRepository).findById(memberId);
        verify(adminMemberRepository).delete(member);
    }

    @Test
    void 단일_회원_삭제_실패_존재하지_않는_회원() {
        // given
        var memberId = 999L;

        given(adminMemberRepository.findById(memberId)).willReturn(Optional.empty());

        // expected
        assertThatThrownBy(() -> adminMemberService.deleteMember(memberId))
                .isInstanceOf(MemberException.class)
                .extracting(e -> ((MemberException) e).getErrorCode())
                .isEqualTo(MemberErrorCode.NOT_FOUND_MEMBER);

        verify(adminMemberRepository).findById(memberId);
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

        given(adminMemberRepository.findAllById(memberIds)).willReturn(members);
        doNothing().when(adminMemberRepository).deleteAll(members);

        // when
        adminMemberService.deleteMembers(memberIds);

        // then
        verify(adminMemberRepository).findAllById(memberIds);
        verify(adminMemberRepository).deleteAll(members);
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

        given(adminMemberRepository.findAllById(memberIds)).willReturn(members);

        // expected
        assertThatThrownBy(() -> adminMemberService.deleteMembers(memberIds))
                .isInstanceOf(MemberException.class)
                .extracting(e -> ((MemberException) e).getErrorCode())
                .isEqualTo(MemberErrorCode.NOT_FOUND_MEMBER);

        verify(adminMemberRepository).findAllById(memberIds);
    }

    @Test
    void 다중_회원_삭제_실패_빈_목록() {
        // given
        List<Long> emptyMemberIds = List.of();
        List<Member> emptyMembers = List.of();

        given(adminMemberRepository.findAllById(emptyMemberIds)).willReturn(emptyMembers);

        // when
        adminMemberService.deleteMembers(emptyMemberIds);

        // then
        verify(adminMemberRepository).findAllById(emptyMemberIds);
        verify(adminMemberRepository).deleteAll(emptyMembers);
    }
}
