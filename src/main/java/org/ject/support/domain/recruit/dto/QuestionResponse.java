package org.ject.support.domain.recruit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;

import java.util.List;

import static org.ject.support.domain.recruit.domain.Question.InputType;

@Builder
public record QuestionResponse(
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        Long id,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        int sequence,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        InputType inputType,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        boolean isRequired,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        String title,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String subtitle,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        String label,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> selectOptions,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String inputHint,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        Integer maxTextLength,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        Integer maxFileSize
) {

    @QueryProjection
    public QuestionResponse {
    }
}