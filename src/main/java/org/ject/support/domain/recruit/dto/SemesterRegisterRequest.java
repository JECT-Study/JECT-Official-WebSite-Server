package org.ject.support.domain.recruit.dto;

import jakarta.validation.Valid;
import java.util.List;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record SemesterRegisterRequest(String name,
                                      @Valid @NotEmpty List<@NotNull RecruitRegisterRequest> recruitRegisterRequests) {
        }
