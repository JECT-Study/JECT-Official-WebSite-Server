package org.ject.support.domain.recruit.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;

import java.util.List;

import static org.ject.support.domain.recruit.domain.Question.InputType;

@Builder
public record QuestionResponse(Long id,
                               int sequence,
                               InputType inputType,
                               boolean isRequired,
                               String title,
                               String label,
                               List<String> selectOptions,
                               String inputHint,
                               Integer maxTextLength,
                               Integer maxFileSize) {

    @QueryProjection
    public QuestionResponse {
    }
}