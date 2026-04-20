package org.ject.support.common.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ject.support.admin.exception.AdminErrorCode;
import org.ject.support.admin.exception.AdminException;
import org.ject.support.common.exception.BusinessException;
import org.ject.support.common.exception.GlobalErrorCode;
import org.ject.support.common.exception.GlobalException;
import org.ject.support.common.security.CustomUserDetails;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.repository.MemberRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JWT 토큰 기반의 인증을 처리하는 필터
 * 
 * 요청 헤더에서 JWT 토큰을 추출
 * 토큰의 유효성 검증
 * 유효한 토큰이면 인증 정보를 SecurityContext에 저장
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Set<String> BACKOFFICE_AUTHORITIES = Role.backofficeRoles().stream()
            .map(role -> "ROLE_" + role.name())
            .collect(Collectors.toUnmodifiableSet());

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {

        try {
            // =========================
            // 1. Access Token 처리 (선택 인증)
            // =========================
            String accessToken = jwtTokenProvider.resolveAccessToken(request);

            if (accessToken != null) {
                if (jwtTokenProvider.validateToken(accessToken)) {
                    Authentication auth = jwtTokenProvider.getAuthenticationByToken(accessToken);
                    validateBackofficeMemberStatus(auth);
                    SecurityContextHolder.getContext().setAuthentication(auth);

                    chain.doFilter(request, response);
                    return;
                } else {
                    clearAuthCookie(response, "accessToken");
                    SecurityContextHolder.clearContext();
                }
            }

            // =========================
            // 2. Verification Token 처리 (의도적 인증)
            // =========================
            String verificationToken =
                    jwtTokenProvider.resolveVerificationToken(request);

            if (verificationToken != null) {
                if (jwtTokenProvider.validateToken(verificationToken)) {
                    String email = jwtTokenProvider.extractEmailFromVerificationToken(verificationToken);

                    Authentication auth = createVerificationAuthentication(email);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } else {
                    clearAuthCookie(response, "verificationToken");
                    SecurityContextHolder.clearContext();
                }
            }

            chain.doFilter(request, response);

        } catch (BusinessException e) {
            SecurityContextHolder.clearContext();
            throw e;
        } catch (Exception e) {
            log.error("JWT 인증 처리 중 에러 발생", e);
            SecurityContextHolder.clearContext();
            throw new GlobalException(GlobalErrorCode.INVALID_ACCESS_TOKEN);
        }
    }

    private void validateBackofficeMemberStatus(Authentication authentication) {
        if (!hasBackofficeRole(authentication)) {
            return;
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        var member = memberRepository.findByIdAndRoleIn(userDetails.getMemberId(), Role.backofficeRoles())
                .orElseThrow(() -> new AdminException(AdminErrorCode.NOT_FOUND_ADMIN));

        if (member.getStatus() == MemberStatus.LOCKED) {
            throw new AdminException(AdminErrorCode.LOCKED_ADMIN);
        }
    }

    private boolean hasBackofficeRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(BACKOFFICE_AUTHORITIES::contains);
    }
    
    private Authentication createVerificationAuthentication(String email) {
        // 임시 인증 정보 생성 (이메일만 포함, 권한은 VERIFICATION 으로 제한)
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_VERIFICATION"));
        
        // String 대신 CustomUserDetails 객체 생성
        CustomUserDetails userDetails = new CustomUserDetails(email, null, Role.VERIFICATION); // 또는 적절한 Role 사용
        
        return new UsernamePasswordAuthenticationToken(
                userDetails, "", authorities);
    }

    private void clearAuthCookie(HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        response.addCookie(cookie);
    }
}
