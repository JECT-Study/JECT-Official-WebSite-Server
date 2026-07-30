package org.ject.support.admin.semester.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateSemesterEventRequest(
        @NotNull(message = "행사 ID를 입력해주세요.")
        Long id,

        @NotBlank(message = "행사 이름을 입력해주세요.")
        @Size(max = 25, message = "행사 이름은 25자 이하여야 합니다.")
        String name
) {
}
