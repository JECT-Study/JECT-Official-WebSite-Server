package org.ject.support.common.security;


import lombok.RequiredArgsConstructor;
import org.ject.support.domain.applicant.entity.Applicant;
import org.ject.support.domain.applicant.exception.ApplicantException;
import org.ject.support.domain.applicant.repository.ApplicantRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static org.ject.support.domain.applicant.exception.ApplicantErrorCode.NOT_FOUND_APPLICANT;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final ApplicantRepository applicantRepository;

    @Override
    public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Applicant applicant = applicantRepository.findByEmail(username)
                .orElseThrow(() -> new ApplicantException(NOT_FOUND_APPLICANT));
        return new CustomUserDetails(applicant);
    }
}
