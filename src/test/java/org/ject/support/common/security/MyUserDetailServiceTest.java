package org.ject.support.common.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Member;
import org.ject.support.domain.member.MemberException;
import org.ject.support.domain.member.MemberRepository;
import org.ject.support.domain.member.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MyUserDetailServiceTest {

    private MyUserDetailService userDetailService;

    @Mock
    private MemberRepository memberRepository;

    private Member testMember;

    @BeforeEach
    void setUp() {
        userDetailService = new MyUserDetailService(memberRepository);
        testMember = Member.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .phoneNumber("01012345678")
                .role(Role.USER)
                .jobFamily(JobFamily.BE)
                .build();
    }

    @Test
    @DisplayName("이메일로 사용자 조회 성공 테스트")
    void loadUserByUsername_Success() {
        // given
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(testMember));

        // when
        CustomUserDetails userDetails = userDetailService.loadUserByUsername("test@example.com");

        // then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(testMember.getEmail());
        assertThat(userDetails.getMemberId()).isEqualTo(testMember.getId());
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 조회 시 예외 발생 테스트")
    void loadUserByUsername_NotFound() {
        // given
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userDetailService.loadUserByUsername("nonexistent@example.com"))
                .isInstanceOf(MemberException.class);
    }
}
