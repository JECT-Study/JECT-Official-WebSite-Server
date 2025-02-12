package org.ject.support.common.security.jwt;


import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.ject.support.common.exception.GlobalException;
import org.ject.support.common.security.CustomUserDetails;
import org.ject.support.domain.member.Member;
import org.ject.support.domain.member.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import static org.ject.support.common.exception.GlobalErrorCode.AUTHENTICATION_REQUIRED;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${spring.jwt.token.access-expiration-time}")
    private long accessExpirationTime;

    @Value("${spring.jwt.token.refresh-expiration-time}")
    private long refreshExpirationTime;

    private final Key secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
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
        Role role = Role.valueOf(claims.get("role", String.class).toUpperCase());
        
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
}