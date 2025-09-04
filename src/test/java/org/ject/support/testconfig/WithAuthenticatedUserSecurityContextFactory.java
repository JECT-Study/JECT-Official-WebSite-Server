package org.ject.support.testconfig;

import org.ject.support.common.security.CustomUserDetails;
import org.ject.support.domain.member.Role;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithAuthenticatedUserSecurityContextFactory implements WithSecurityContextFactory<AuthenticatedUser> {
    @Override
    public SecurityContext createSecurityContext(AuthenticatedUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        String email = annotation.email();
        boolean isAdmin = annotation.isAdmin();
        long memberId = annotation.memberId();

        Role role = isAdmin ? Role.ADMIN : Role.CORE;
        CustomUserDetails userDetails = new CustomUserDetails(email, memberId, role);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());

        context.setAuthentication(auth);
        return context;
    }
}
