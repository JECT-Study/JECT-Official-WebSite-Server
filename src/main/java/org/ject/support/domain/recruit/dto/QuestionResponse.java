package org.ject.support.domain.recruit.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;

import static org.ject.support.domain.recruit.domain.Question.InputType;

@Builder
public record QuestionResponse(Long id,
                               int sequence,
                               InputType inputType,
                               boolean isRequired,
                               String title,
                               String body,
                               String inputHint,
                               Integer maxLength) {

    @QueryProjection
    public QuestionResponse {
    }
}