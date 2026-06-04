package org.ject.support.common.security;

import java.util.ArrayList;
import java.util.Collection;
import org.ject.support.domain.applicant.entity.Applicant;
import org.ject.support.domain.member.Permission;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.RolePermissions;
import org.ject.support.domain.member.entity.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomUserDetails implements UserDetails {

    private final Long applicantId;
    private final String email;
    private final Role role;

    public CustomUserDetails(Applicant applicant) {
        this.applicantId = applicant.getId();
        this.email = applicant.getEmail();
        this.role = applicant.getRole();
    }

    public CustomUserDetails(Member member) {
        this.applicantId = member.getId();
        this.email = member.getEmail();
        this.role = member.getRole();
    }

    public CustomUserDetails(String email, Long applicantId, Role role) {
        this.email = email;
        this.applicantId = applicantId;
        this.role = role;
    }

    public Long getApplicantId() {
        return applicantId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add((GrantedAuthority)() -> "ROLE_" + role);
        RolePermissions.getPermissions(role).stream()
                .map(Permission::name)
                .map(permission -> (GrantedAuthority)() -> permission)
                .forEach(collection::add);
        return collection;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return email;
    }

}
