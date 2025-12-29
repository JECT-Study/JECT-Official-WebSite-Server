package org.ject.support.common.springdoc;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.ject.support.common.exception.ErrorCode;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ErrorResponseCustomizer implements OperationCustomizer {

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        ApiErrorResponses methodAnnotation = handlerMethod.getMethodAnnotation(ApiErrorResponses.class);

        if (methodAnnotation != null) {
            ApiErrorResponse[] apiErrorResponses = methodAnnotation.responses();
            ApiResponses responses = operation.getResponses();
            Map<Integer, List<ExampleHolder>> exampleHolders = createExampleHolders(apiErrorResponses);
            addExampleHoldersToResponses(responses, exampleHolders);
        }

        return operation;
    }

    private Map<Integer, List<ExampleHolder>> createExampleHolders(ApiErrorResponse[] apiErrorResponses) {
        return Arrays.stream(apiErrorResponses)
                .map(apiErrorResponse -> {
                    ErrorCode errorCode = getErrorCode(apiErrorResponse);
                    return createExampleHolder(apiErrorResponse, errorCode);
                })
                .collect(Collectors.groupingBy(ExampleHolder::code));
    }

    @SuppressWarnings("unchecked")
    private <E extends Enum<E> & ErrorCode> E getErrorCode(ApiErrorResponse annotation) {
        try {
            return Enum.valueOf((Class<E>) annotation.value(), annotation.name());
        } catch (IllegalArgumentException ex) {
            log.warn("Unknown ErrorCode enum constant: {}#{}", annotation.value(), annotation.name());
            return null;
        }
    }

    private ExampleHolder createExampleHolder(ApiErrorResponse apiErrorResponse, ErrorCode errorCode) {
        Example example = new Example();
        if (errorCode == null) {
            example.value(null);
            example.description("'%s' is unknown".formatted(apiErrorResponse.name()));
            return ExampleHolder.builder()
                    .example(example)
                    .code(0)
                    .name(apiErrorResponse.name())
                    .build();
        }
        example.value(new org.ject.support.common.response.ApiResponse<>(errorCode.getCode(), errorCode.getMessage()));
        example.description(apiErrorResponse.description());
        return ExampleHolder.builder()
                .example(example)
                .code(errorCode.getHttpStatus().value())
                .name(apiErrorResponse.name())
                .build();
    }

    private void addExampleHoldersToResponses(ApiResponses responses, Map<Integer, List<ExampleHolder>> exampleHolders) {
        exampleHolders.forEach((code, v) -> {
                    Content content = new Content();
                    MediaType mediaType = new MediaType();
                    ApiResponse apiResponse = new ApiResponse();

                    v.forEach(exampleHolder ->
                            mediaType.addExamples(exampleHolder.name(), exampleHolder.example()));
                    content.addMediaType("application/json", mediaType);
                    apiResponse.setContent(content);
                    responses.addApiResponse(String.valueOf(code), apiResponse);
                }
        );
    }

    @Builder
    private record ExampleHolder(Example example, int code, String name) {
    }
}
