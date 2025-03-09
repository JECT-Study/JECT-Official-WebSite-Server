package org.ject.support.common.security.jwt;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Date;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.ject.support.common.exception.GlobalException;
import org.ject.support.common.security.CustomUserDetails;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.entity.Member;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import static org.ject.support.common.exception.GlobalErrorCode.AUTHENTICATION_REQUIRED;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    /**
     * -- GETTER --
     *  JWT 서명에 사용되는 비밀키 반환
     */

    @Getter
    private final Key secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    @Value("${spring.jwt.token.access-expiration-time}")
    private long accessExpirationTime;
    @Value("${spring.jwt.token.refresh-expiration-time}")
    private long refreshExpirationTime;

    /**
     * Access 토큰 생성
     */
    public String createAccessToken(Authentication authentication, Long memberId) {
        validateAuthentication(authentication);
        Claims claims = Jwts.claims();
        claims.put("memberId", memberId);
        String role = ((CustomUserDetails) authentication.getPrincipal()).getAuthorities().iterator().next().getAuthority();
        claims.put("role", role);
        claims.setSubject(authentication.getName());
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + accessExpirationTime);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Refresh 토큰 생성
     */
    public String createRefreshToken(Authentication authentication) {
        validateAuthentication(authentication);
        Claims claims = Jwts.claims();
        claims.setSubject(authentication.getName());
        String role = ((CustomUserDetails) authentication.getPrincipal()).getAuthorities().iterator().next().getAuthority();
        claims.put("role", role);
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + refreshExpirationTime);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(secretKey)
                .compact();
    }


    public Authentication getAuthenticationByToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
        String email = claims.getSubject();
        Long memberId = claims.get("memberId", Long.class);
        String roleStr = claims.get("role", String.class).toUpperCase();
        
        // "ROLE_" 접두사가 있으면 제거
        if (roleStr.startsWith("ROLE_")) {
            roleStr = roleStr.substring(5);
        }
        
        Role role = Role.valueOf(roleStr);

        CustomUserDetails userDetails = new CustomUserDetails(email, memberId, role);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private void validateAuthentication(Authentication authentication) {
        if (authentication == null) {
            throw new GlobalException(AUTHENTICATION_REQUIRED);
        }
    }

    public String reissueAccessToken(String refreshToken, Long memberId) {
        validateToken(refreshToken);
        Authentication authentication = getAuthenticationByToken(refreshToken);
        return createAccessToken(authentication, memberId);
    }

    public Long extractMemberId(String refreshToken) {
        return getMemberId(refreshToken);
    }

    public Long getMemberId(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
        return claims.get("memberId", Long.class);
    }

    public Authentication createAuthenticationByMember(Member member) {
        CustomUserDetails userDetails = new CustomUserDetails(member);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }
    
    /**
     * 인증번호 검증이 완료된 사용자를 위한 임시 토큰 발급
     * 이 토큰은 회원 정보를 포함하지 않고, 인증번호 검증이 완료되었음을 증명하는 용도로만 사용
     * 유효기간은 일반 토큰보다 짧게 설정
     */
    public String createVerificationToken(String email) {
        Claims claims = Jwts.claims();
        claims.setSubject(email);
        claims.put("type", "verification");
        Date now = new Date();
        // 인증번호 검증 후 토큰 유효기간은 10분으로 설정
        Date expireDate = new Date(now.getTime() + 600000); // 10분

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(secretKey)
                .compact();
    }
    
    /**
     * 인증번호 검증 토큰에서 이메일 추출
     */
    public String extractEmailFromVerificationToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
        String type = claims.get("type", String.class);
        if (!"verification".equals(type)) {
            throw new JwtException("유효하지 않은 토큰 타입입니다.");
        }
        return claims.getSubject();
    }
}