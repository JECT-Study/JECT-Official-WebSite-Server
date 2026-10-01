package org.ject.support.domain.applicant.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

public record DeleteApplicantsRequest(
        @NotEmpty Set<@NotNull Long> applicantIds
) {}
