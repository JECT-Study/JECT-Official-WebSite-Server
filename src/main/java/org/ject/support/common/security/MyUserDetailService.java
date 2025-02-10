package org.ject.support.common.security;


import static org.ject.support.domain.member.MemberErrorCode.NOT_FOUND_MEMBER;

import lombok.RequiredArgsConstructor;
import org.ject.support.domain.member.Member;
import org.ject.support.domain.member.MemberException;
import org.ject.support.domain.member.MemberRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyUserDetailService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(username)
                .orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));
        return new CustomUserDetails(member);
    }
}
