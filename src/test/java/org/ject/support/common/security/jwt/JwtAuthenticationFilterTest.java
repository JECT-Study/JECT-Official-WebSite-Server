package org.ject.support.common.security.jwt;

import jakarta.servlet.FilterChain;
import org.ject.support.admin.exception.AdminErrorCode;
import org.ject.support.admin.exception.AdminException;
import org.ject.support.common.security.CustomUserDetails;
import org.ject.support.domain.applicant.entity.Applicant;
import org.ject.support.domain.applicant.repository.ApplicantRepository;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private ApplicantRepository applicantRepository;

    @Mock
    private FilterChain filterChain;

    private static final String ACCESS_TOKEN = "access-token";

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void 백오피스_계정이_ACTIVE이면_인증을_처리한다() throws Exception {
        // given
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        var authentication = createAuthentication(1L, Role.ADMIN);
        var applicant = createApplicant(1L, Role.ADMIN, MemberStatus.ACTIVE);

        given(jwtTokenProvider.resolveAccessToken(request)).willReturn(ACCESS_TOKEN);
        given(jwtTokenProvider.validateToken(ACCESS_TOKEN)).willReturn(true);
        given(jwtTokenProvider.getAuthenticationByToken(ACCESS_TOKEN)).willReturn(authentication);
        given(applicantRepository.findByIdAndRoleIn(1L, Role.backofficeRoles())).willReturn(Optional.of(applicant));

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(authentication);
    }

    @Test
    void 백오피스_계정이_LOCKED이면_인증을_차단한다() throws Exception {
        // given
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        var authentication = createAuthentication(1L, Role.ADMIN);
        var applicant = createApplicant(1L, Role.ADMIN, MemberStatus.LOCKED);

        given(jwtTokenProvider.resolveAccessToken(request)).willReturn(ACCESS_TOKEN);
        given(jwtTokenProvider.validateToken(ACCESS_TOKEN)).willReturn(true);
        given(jwtTokenProvider.getAuthenticationByToken(ACCESS_TOKEN)).willReturn(authentication);
        given(applicantRepository.findByIdAndRoleIn(1L, Role.backofficeRoles())).willReturn(Optional.of(applicant));

        // when, then
        assertThatThrownBy(() -> jwtAuthenticationFilter.doFilterInternal(request, response, filterChain))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.LOCKED_ADMIN);

        verify(filterChain, never()).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void 백오피스_계정이_아니면_상태를_조회하지_않는다() throws Exception {
        // given
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        var authentication = createAuthentication(1L, Role.APPLY);

        given(jwtTokenProvider.resolveAccessToken(request)).willReturn(ACCESS_TOKEN);
        given(jwtTokenProvider.validateToken(ACCESS_TOKEN)).willReturn(true);
        given(jwtTokenProvider.getAuthenticationByToken(ACCESS_TOKEN)).willReturn(authentication);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(applicantRepository, never()).findByIdAndRoleIn(1L, Role.backofficeRoles());
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(authentication);
    }

    private UsernamePasswordAuthenticationToken createAuthentication(final Long applicantId,
                                                                    final Role role) {
        CustomUserDetails userDetails = new CustomUserDetails("test@ject.kr", applicantId, role);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    private Applicant createApplicant(final Long applicantId,
                                      final Role role,
                                      final MemberStatus status) {
        return Applicant.builder()
                .id(applicantId)
                .email("test@ject.kr")
                .role(role)
                .status(status)
                .semesterId(1L)
                .build();
    }
}
