package org.ject.support.domain.applicant.repository;

import org.ject.support.admin.account.dto.AdminAccountSearchCondition;
import org.ject.support.domain.applicant.dto.ApplicantAccountProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ApplicantQueryRepository {

    List<String> findEmailsByIdsAndNotSubmitted(List<Long> applicantIds);

    Page<ApplicantAccountProjection> findAccounts(AdminAccountSearchCondition condition, Pageable pageable);
}
