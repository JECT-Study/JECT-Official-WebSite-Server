package org.ject.support.domain.member;

import org.ject.support.domain.member.entity.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MemberTest {

    @Test
    @DisplayName("Member 엔티티 생성 성공")
    void createMember_Success() {
        // given
        String name = "John Doe";
        String phoneNumber = "01012345678";
        String email = "john@example.com";
        JobFamily jobFamily = JobFamily.BE;
        Role role = Role.SEMESTER;
        Region region = Region.SEOUL;

        // when
        Member member = Member.builder()
                .name(name)
                .phoneNumber(phoneNumber)
                .email(email)
                .jobFamily(jobFamily)
                .role(role)
                .region(region)
                .status(MemberStatus.ACTIVE)
                .build();

        // then
        assertThat(member.getName()).isEqualTo(name);
        assertThat(member.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(member.getEmail()).isEqualTo(email);
        assertThat(member.getJobFamily()).isEqualTo(jobFamily);
        assertThat(member.getRole()).isEqualTo(role);
        assertThat(member.getRegion()).isEqualTo(region);
    }

    @Test
    @DisplayName("Member 엔티티 생성 - 최소 필수 필드만으로 생성")
    void createMember_WithRequiredFieldsOnly_Success() {
        // given
        String name = "John Doe";
        String phoneNumber = "01012345678";
        String email = "john@example.com";
        Role role = Role.SEMESTER;
        Region region = Region.OVERSEAS;

        // when
        Member member = Member.builder()
                .name(name)
                .phoneNumber(phoneNumber)
                .email(email)
                .role(role)
                .region(region)
                .status(MemberStatus.ACTIVE)
                .build();

        // then
        assertThat(member.getName()).isEqualTo(name);
        assertThat(member.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(member.getEmail()).isEqualTo(email);
        assertThat(member.getRole()).isEqualTo(role);
        assertThat(member.getRegion()).isEqualTo(region);
        assertThat(member.getJobFamily()).isNull();
    }
}
