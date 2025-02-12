package org.ject.support.domain.member;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberTest {

    @Test
    @DisplayName("Member 엔티티 생성 성공")
    void createMember_Success() {
        // given
        String name = "John Doe";
        String phoneNumber = "01012345678";
        String email = "john@example.com";
        String semester = "2024-1";
        JobFamily jobFamily = JobFamily.BE;
        Role role = Role.USER;

        // when
        Member member = Member.builder()
            .name(name)
            .phoneNumber(phoneNumber)
            .email(email)
            .semester(semester)
            .jobFamily(jobFamily)
            .role(role)
            .build();

        // then
        assertThat(member.getName()).isEqualTo(name);
        assertThat(member.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(member.getEmail()).isEqualTo(email);
        assertThat(member.getSemester()).isEqualTo(semester);
        assertThat(member.getJobFamily()).isEqualTo(jobFamily);
        assertThat(member.getRole()).isEqualTo(role);
    }

    @Test
    @DisplayName("Member 엔티티 생성 - 최소 필수 필드만으로 생성")
    void createMember_WithRequiredFieldsOnly_Success() {
        // given
        String name = "John Doe";
        String phoneNumber = "01012345678";
        String email = "john@example.com";
        Role role = Role.USER;

        // when
        Member member = Member.builder()
            .name(name)
            .phoneNumber(phoneNumber)
            .email(email)
            .role(role)
            .build();

        // then
        assertThat(member.getName()).isEqualTo(name);
        assertThat(member.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(member.getEmail()).isEqualTo(email);
        assertThat(member.getRole()).isEqualTo(role);
        assertThat(member.getSemester()).isNull();
        assertThat(member.getJobFamily()).isNull();
    }
}
