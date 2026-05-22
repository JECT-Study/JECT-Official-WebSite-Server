package org.ject.support.domain.member.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import org.ject.support.admin.account.dto.AdminAccountSearchCondition;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.MemberType;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.dto.MemberAccountProjection;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.testconfig.QueryDslTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@Import(QueryDslTestConfig.class)
@DataJpaTest
class MemberAccountQueryRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Test
    void 필터가_없으면_삭제되지_않은_백오피스_계정만_조회한다() {
        // given
        Member admin = createMember("admin@ject.kr", "관리자", Role.ADMIN, MemberStatus.ACTIVE);
        Member operations = createMember("operations@ject.kr", "운영자", Role.OPERATIONS, MemberStatus.ACTIVE);
        Member semester = createMember("semester@ject.kr", "일반회원", Role.SEMESTER, MemberStatus.ACTIVE);
        Member deleted = createMember("deleted@ject.kr", "삭제회원", Role.SUPPORTER, MemberStatus.ACTIVE);
        ReflectionTestUtils.setField(deleted, "isDeleted", true);
        memberRepository.saveAll(List.of(admin, operations, semester, deleted));

        // when
        Page<MemberAccountProjection> result = memberRepository.findAccounts(
                new AdminAccountSearchCondition(null, null),
                PageRequest.of(0, 20));

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(MemberAccountProjection::email)
                .containsExactlyInAnyOrder("admin@ject.kr", "operations@ject.kr");
    }

    @Test
    void 계정_유형과_상태를_복수_필터로_조회한다() {
        // given
        memberRepository.saveAll(List.of(
                createMember("active-admin@ject.kr", "활성관리자", Role.ADMIN, MemberStatus.ACTIVE),
                createMember("locked-admin@ject.kr", "잠긴관리자", Role.ADMIN, MemberStatus.LOCKED),
                createMember("active-supporter@ject.kr", "활성서포터", Role.SUPPORTER, MemberStatus.ACTIVE),
                createMember("locked-operations@ject.kr", "잠긴운영자", Role.OPERATIONS, MemberStatus.LOCKED)
        ));

        AdminAccountSearchCondition condition = new AdminAccountSearchCondition(
                List.of(Role.ADMIN, Role.SUPPORTER),
                List.of(MemberStatus.ACTIVE));

        // when
        Page<MemberAccountProjection> result = memberRepository.findAccounts(condition, PageRequest.of(0, 20));

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(MemberAccountProjection::email)
                .containsExactlyInAnyOrder("active-admin@ject.kr", "active-supporter@ject.kr");
    }

    @Test
    void 페이지네이션과_기본_정렬을_적용한다() {
        // given
        Member first = createMember("first@ject.kr", "첫번째", Role.ADMIN, MemberStatus.ACTIVE);
        Member second = createMember("second@ject.kr", "두번째", Role.ADMIN, MemberStatus.ACTIVE);
        Member third = createMember("third@ject.kr", "세번째", Role.ADMIN, MemberStatus.ACTIVE);
        setCreatedAt(first, LocalDateTime.of(2026, 1, 1, 0, 0));
        setCreatedAt(second, LocalDateTime.of(2026, 1, 2, 0, 0));
        setCreatedAt(third, LocalDateTime.of(2026, 1, 3, 0, 0));
        memberRepository.saveAll(List.of(first, second, third));
        memberRepository.flush();

        // when
        Page<MemberAccountProjection> result = memberRepository.findAccounts(
                new AdminAccountSearchCondition(null, null),
                PageRequest.of(0, 2));

        // then
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent())
                .extracting(MemberAccountProjection::email)
                .containsExactly("third@ject.kr", "second@ject.kr");
    }

    private Member createMember(String email, String name, Role role, MemberStatus status) {
        return Member.builder()
                .email(email)
                .name(name)
                .phoneNumber("01012345678")
                .memberType(MemberType.SEMESTER)
                .semesterId(1L)
                .role(role)
                .pin("encoded-password")
                .status(status)
                .build();
    }

    private void setCreatedAt(Member member, LocalDateTime createdAt) {
        ReflectionTestUtils.setField(member, "createdAt", createdAt);
    }
}
