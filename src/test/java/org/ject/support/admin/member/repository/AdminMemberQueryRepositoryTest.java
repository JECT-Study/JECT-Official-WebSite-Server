package org.ject.support.admin.member.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.MemberType;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.dto.MemberProjection;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.repository.SemesterRepository;
import org.ject.support.testconfig.QueryDslTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Import({QueryDslTestConfig.class, AdminMemberRepositoryImpl.class})
@DataJpaTest
class AdminMemberQueryRepositoryTest {

    @Autowired
    AdminMemberRepository adminMemberRepository;

    @Autowired
    SemesterRepository semesterRepository;

    Semester semester;

    @BeforeEach
    void setUp() {
        semester = semesterRepository.save(Semester.builder()
                .name("1기")
                .isRecruiting(false)
                .build());
    }

    @Test
    void 구성원_타입으로_회원_목록을_필터링한다() {
        // given
        Member semesterMember = createMember("semester@test.com", MemberType.SEMESTER);
        Member makersMember = createMember("makers@test.com", MemberType.MAKERS);
        adminMemberRepository.saveAll(List.of(semesterMember, makersMember));

        // when
        Page<MemberProjection> result = adminMemberRepository.findMembers(
                Role.SEMESTER, null, null, MemberType.MAKERS, PageRequest.of(0, 15));

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().email()).isEqualTo("makers@test.com");
    }

    @Test
    void 구성원_타입이_null이면_기존_조건으로_회원_목록을_조회한다() {
        // given
        Member semesterMember = createMember("semester@test.com", MemberType.SEMESTER);
        Member makersMember = createMember("makers@test.com", MemberType.MAKERS);
        adminMemberRepository.saveAll(List.of(semesterMember, makersMember));

        // when
        Page<MemberProjection> result = adminMemberRepository.findMembers(
                Role.SEMESTER, null, null, null, PageRequest.of(0, 15));

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    private Member createMember(String email, MemberType memberType) {
        return Member.builder()
                .email(email)
                .name("홍길동")
                .phoneNumber("01012345678")
                .semesterId(semester.getId())
                .jobFamily(JobFamily.BE)
                .role(Role.SEMESTER)
                .memberType(memberType)
                .pin("123456")
                .status(MemberStatus.ACTIVE)
                .build();
    }
}
