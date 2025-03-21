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
        String token = jwtTokenProvider.resolveToken(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            try {
                // 토큰 타입 확인
                if (isVerificationToken(token)) {
                    // verification 토큰인 경우 별도 처리
                    // 이메일만 추출하여 간단한 인증 정보 생성
                    String email = jwtTokenProvider.extractEmailFromVerificationToken(token);
                    Authentication auth = createVerificationAuthentication(email);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } else {
                    // 일반 인증 토큰인 경우 기존 방식으로 처리
                    Authentication auth = jwtTokenProvider.getAuthenticationByToken(token);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception e) {
                log.error("토큰 처리 중 오류 발생: {}", e.getMessage());
                // 토큰 처리 중 오류가 발생해도 요청은 계속 진행
            }
        }

        chain.doFilter(request, response);
    }
    
    /**
     * 주어진 토큰이 verification 토큰인지 확인
     */
    private boolean isVerificationToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtTokenProvider.getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            // type 필드가 있는지 확인
            if (claims.containsKey("type")) {
                String type = claims.get("type", String.class);
                return "verification".equals(type);
            }
            return false;
        } catch (Exception e) {
            log.debug("토큰 타입 확인 중 예외 발생: {}", e.getMessage());
            return false;
        }
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

