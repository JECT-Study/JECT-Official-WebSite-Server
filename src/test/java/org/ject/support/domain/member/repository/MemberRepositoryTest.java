package org.ject.support.domain.member.repository;

import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.testconfig.QueryDslTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Import(QueryDslTestConfig.class)
@DataJpaTest
class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;

    @Test
    void 이메일과_역활로_회원을_조회시_존재하는_회원을_반환한다() {
        // given
        String email = "test@example.com";
        Role role = Role.ADMIN;
        Member member = createMember("테스트", "01012345678", email, JobFamily.FE, role);
        memberRepository.save(member);

        // when
        Optional<Member> found = memberRepository.findByEmailAndRole(email, role);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(email);
        assertThat(found.get().getRole()).isEqualTo(role);
    }

    @Test
    void 존재하지_않는_이메일과_역활로_회원을_조회시_빈_Optional을_반환한다() {
        // given
        String findEmail = "notfound@example.com";
        Role findRole = Role.ADMIN;

        // when
        Optional<Member> found = memberRepository.findByEmailAndRole(findEmail, findRole);

        // then
        assertThat(found).isEmpty();
    }

    private Member createMember(String name, String phoneNumber, String email, JobFamily jobFamily, Role role) {
        return Member.builder()
                .name(name)
                .phoneNumber(phoneNumber)
                .email(email)
                .semesterId(1L)
                .jobFamily(jobFamily)
                .role(role)
                .pin("123456")
                .status(MemberStatus.ACTIVE)
                .build();
    }
}
