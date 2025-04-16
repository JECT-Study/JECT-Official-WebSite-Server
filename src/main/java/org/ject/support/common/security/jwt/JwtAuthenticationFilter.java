package org.ject.support.common.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.ject.support.common.security.CustomUserDetails;
import org.ject.support.domain.member.Role;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

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

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String accessToken = jwtTokenProvider.resolveAccessToken(request);

        if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
            try {
                Authentication auth = jwtTokenProvider.getAuthenticationByToken(accessToken);
                SecurityContextHolder.getContext().setAuthentication(auth);
                chain.doFilter(request, response);
                return;
            } catch (Exception e) {
                log.error("엑세스 토큰 인증 실패: {}", e.getMessage());
            }
        }

        String verificationToken = jwtTokenProvider.resolveVerificationToken(request);
        if (verificationToken != null && jwtTokenProvider.validateToken(verificationToken)) {
            try {
                // verification 토큰에서 이메일 추출
                String email = jwtTokenProvider.extractEmailFromVerificationToken(verificationToken);
                Authentication auth = createVerificationAuthentication(email);
                SecurityContextHolder.getContext().setAuthentication(auth);
                chain.doFilter(request, response);
                return;
            } catch (Exception e) {
                log.error("검증 토큰 인증 실패: {}", e.getMessage());
            }
        }

        // 두 토큰 모두 없거나 유효하지 않으면, 인증 없이 진행
        chain.doFilter(request, response);
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
}

