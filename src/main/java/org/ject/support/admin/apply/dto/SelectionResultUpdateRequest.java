package org.ject.support.admin.apply.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.ject.support.domain.apply.domain.SelectionResult;

@Schema(description = "지원서 선정 결과 일괄 변경 요청")
public record SelectionResultUpdateRequest(
        @Schema(description = "모집 공고 ID", example = "1")
        @NotNull @Positive Long recruitId,
        @Schema(description = "지원서별 선정 결과")
        @NotEmpty List<@NotNull @Valid SelectionResultItem> selectionResults
) {

    public record SelectionResultItem(
            @Schema(description = "지원서 ID", example = "1")
            @NotNull @Positive Long applyId,
            @Schema(description = "선정 결과", example = "PASSED")
            @NotNull SelectionResult selectionResult,
            @Schema(description = "예비 번호. 예비 합격일 때만 입력", example = "1", nullable = true)
            @Positive Integer waitlistNumber
    ) {
    }
}
