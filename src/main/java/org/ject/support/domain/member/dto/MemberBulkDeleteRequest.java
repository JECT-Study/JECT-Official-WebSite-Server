package org.ject.support.domain.member.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record MemberBulkDeleteRequest(
        @NotEmpty List<Long> memberIds
) {}