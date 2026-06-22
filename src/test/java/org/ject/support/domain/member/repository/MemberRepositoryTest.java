package org.ject.support.domain.member.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ject.support.domain.member.fixture.MemberFixture.member;

import java.util.Optional;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.testconfig.QueryDslTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@Import(QueryDslTestConfig.class)
@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Test
    void 이메일로_구성원을_조회한다() {
        // given
        String email = "member@test.com";
        memberRepository.save(member().email(email).build());

        // when
        Optional<Member> result = memberRepository.findByEmail(email);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(email);
    }

    @Test
    void 존재하지_않는_이메일로_조회하면_빈_결과를_반환한다() {
        // given
        String email = "not-found@test.com";

        // when
        Optional<Member> result = memberRepository.findByEmail(email);

        // then
        assertThat(result).isEmpty();
    }
}
