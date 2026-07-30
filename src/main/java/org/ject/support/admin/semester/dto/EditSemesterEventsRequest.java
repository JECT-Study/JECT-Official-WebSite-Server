package org.ject.support.admin.semester.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.ject.support.domain.recruit.domain.SemesterEventType;

public record EditSemesterEventsRequest(
        @NotNull(message = "행사 타입을 입력해주세요.")
        SemesterEventType type,

        @Size(min = 1, message = "추가 행사 목록은 비어 있을 수 없습니다.")
        List<@NotNull(message = "추가 행사 항목은 null일 수 없습니다.") @Valid CreateSemesterEventRequest> created,

        @Size(min = 1, message = "수정 행사 목록은 비어 있을 수 없습니다.")
        List<@NotNull(message = "수정 행사 항목은 null일 수 없습니다.") @Valid UpdateSemesterEventRequest> updated
) {

    // 추가 또는 수정 요청이 하나 이상 존재하는지 검증
    @JsonIgnore
    @AssertTrue(message = "추가 또는 수정 행사 목록 중 하나는 반드시 입력해야 합니다.")
    public boolean isChanged() {
        return created != null || updated != null;
    }

    // 서비스에서 사용할 추가 행사 목록 반환
    public List<CreateSemesterEventRequest> createdOrEmpty() {
        return created == null ? List.of() : created;
    }

    // 서비스에서 사용할 수정 행사 목록 반환
    public List<UpdateSemesterEventRequest> updatedOrEmpty() {
        return updated == null ? List.of() : updated;
    }
}
