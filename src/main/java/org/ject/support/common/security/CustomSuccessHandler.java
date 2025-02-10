package org.ject.support.common.security;


import static org.ject.support.common.security.jwt.JwtTokenProvider.createAccessCookie;
import static org.ject.support.common.security.jwt.JwtTokenProvider.createRefreshCookie;
import static org.ject.support.domain.member.MemberErrorCode.NOT_FOUND_MEMBER;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.ject.support.common.security.jwt.JwtTokenProvider;
import org.ject.support.domain.member.Member;
import org.ject.support.domain.member.MemberException;
import org.ject.support.domain.member.MemberRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {


        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        String username = customUserDetails.getUsername();
        Member member = memberRepository.findByEmail(username)
                .orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));

        String accessToken = jwtTokenProvider.createAccessToken(authentication, member.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

        response.addCookie(createRefreshCookie(refreshToken));
        response.addCookie(createAccessCookie(accessToken));
    }
}
