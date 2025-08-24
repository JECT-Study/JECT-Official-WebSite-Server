package org.ject.support.domain.member.repository;

import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.testconfig.QueryDslTestConfig;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("이메일과 역활로 회원을 조회시 존재하는 회원을 반환한다")
    void findByEmailAndRole_find() {
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
    @DisplayName("존재하지 않는 이메일과 역활로 회원을 조회시 빈 Optional을 반환한다")
    void testFindByEmailAndRole_NoMatch() {
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
                .pin("123456") // PIN 필드 추가
                .build();
    }
}