package org.ject.support.domain.applicant;

import org.ject.support.domain.applicant.entity.Applicant;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.MemberType;
import org.ject.support.domain.member.Region;
import org.ject.support.domain.member.Role;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApplicantTest {

    @Test
    void 지원자_엔티티_생성에_성공() {
        // given
        String name = "John Doe";
        String phoneNumber = "01012345678";
        String email = "john@example.com";
        JobFamily jobFamily = JobFamily.BE;
        Role role = Role.SEMESTER;
        Region region = Region.SEOUL;

        // when
        Applicant applicant = Applicant.builder()
                .name(name)
                .phoneNumber(phoneNumber)
                .email(email)
                .jobFamily(jobFamily)
                .role(role)
                .region(region)
                .status(MemberStatus.ACTIVE)
                .build();

        // then
        assertThat(applicant.getName()).isEqualTo(name);
        assertThat(applicant.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(applicant.getEmail()).isEqualTo(email);
        assertThat(applicant.getJobFamily()).isEqualTo(jobFamily);
        assertThat(applicant.getMemberType()).isEqualTo(MemberType.SEMESTER);
        assertThat(applicant.getRole()).isEqualTo(role);
        assertThat(applicant.getRegion()).isEqualTo(region);
    }

    @Test
    void 지원자_엔티티_최소_필수_필드로_생성에_성공() {
        // given
        String name = "John Doe";
        String phoneNumber = "01012345678";
        String email = "john@example.com";
        Role role = Role.SEMESTER;
        Region region = Region.OVERSEAS;

        // when
        Applicant applicant = Applicant.builder()
                .name(name)
                .phoneNumber(phoneNumber)
                .email(email)
                .role(role)
                .region(region)
                .status(MemberStatus.ACTIVE)
                .build();

        // then
        assertThat(applicant.getName()).isEqualTo(name);
        assertThat(applicant.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(applicant.getEmail()).isEqualTo(email);
        assertThat(applicant.getRole()).isEqualTo(role);
        assertThat(applicant.getRegion()).isEqualTo(region);
        assertThat(applicant.getJobFamily()).isNull();
        assertThat(applicant.getMemberType()).isEqualTo(MemberType.SEMESTER);
    }

    @Test
    void 구성원_타입을_null로_변경하면_예외가_발생한다() {
        // given
        Applicant applicant = Applicant.builder()
                .email("john@example.com")
                .role(Role.SEMESTER)
                .status(MemberStatus.ACTIVE)
                .build();

        // when, then
        assertThatThrownBy(() -> applicant.updateMemberType(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("memberType must not be null");
    }

    @Test
    void 지원자의_이름이_없을_경우_프로필_완성_확인에_FALSE_반환() {
        // given
        Applicant applicant = Applicant.builder()
                .phoneNumber("01012345678")
                .email("noName@ject.com")
                .build();

        // when
        boolean result = applicant.isProfileComplete();

        // then
        assertThat(result).isFalse();
    }

    @Test
    void 지원자의_이름과_핸드폰번호가_있을_경우_프로필_완성_확인에_TRUE_반환() {
        // given
        Applicant applicant = Applicant.builder()
                .name("이름")
                .phoneNumber("01012345678")
                .build();

        // when
        boolean result = applicant.isProfileComplete();

        // then
        assertThat(result).isTrue();
    }
}
