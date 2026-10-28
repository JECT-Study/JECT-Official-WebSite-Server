package org.ject.support.admin.apply.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.ject.support.domain.apply.domain.SelectionResult;

public record UpdateSelectionResultsRequest(
        @NotNull @Positive Long recruitId,
        @NotEmpty List<@NotNull @Positive Long> applyIds,
        @NotNull SelectionResult selectionResult
) {
    @JsonIgnore
    @AssertTrue(message = "합격, 불합격, 예비 합격만 지정할 수 있습니다.")
    public boolean isSelectionResultAllowed() {
        return selectionResult == null || selectionResult != SelectionResult.UNDECIDED;
    }
}
