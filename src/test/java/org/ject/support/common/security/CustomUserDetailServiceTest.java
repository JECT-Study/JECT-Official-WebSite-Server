package org.ject.support.common.security;

import org.ject.support.domain.applicant.entity.Applicant;
import org.ject.support.domain.applicant.exception.ApplicantException;
import org.ject.support.domain.applicant.repository.ApplicantRepository;
import org.ject.support.domain.member.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailServiceTest {

    @InjectMocks
    private CustomUserDetailService userDetailService;

    @Mock
    private ApplicantRepository applicantRepository;

    private Applicant testApplicant;

    @BeforeEach
    void setUp() {
        testApplicant = Applicant.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .phoneNumber("01012345678")
                .role(Role.SEMESTER)
                .build();
    }

    @Test
    @DisplayName("이메일로 사용자 조회 성공 테스트")
    void loadUserByUsername_Success() {
        // given
        when(applicantRepository.findByEmail(anyString())).thenReturn(Optional.of(testApplicant));

        // when
        CustomUserDetails userDetails = userDetailService.loadUserByUsername("test@example.com");

        // then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(testApplicant.getEmail());
        assertThat(userDetails.getApplicantId()).isEqualTo(testApplicant.getId());
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 조회 시 예외 발생 테스트")
    void loadUserByUsername_NotFound() {
        // given
        when(applicantRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userDetailService.loadUserByUsername("nonexistent@example.com"))
                .isInstanceOf(ApplicantException.class);
    }
}
