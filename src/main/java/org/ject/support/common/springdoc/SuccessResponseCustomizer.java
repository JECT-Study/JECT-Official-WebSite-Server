package org.ject.support.common.springdoc;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import java.util.List;

import static org.ject.support.common.response.ApiResponseConstant.SUCCESS_STATUS_MESSAGE;

@Component
public class SuccessResponseCustomizer implements OperationCustomizer {

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        ApiResponses responses = operation.getResponses();
        if (handlerMethod != null && handlerMethod.getReturnType().getParameterType() == void.class) {
            responses.forEach((code, response) -> {
                if ((code.startsWith("2") && !code.equals("204")) || code.equals("default")) {
                    if (response.getContent() == null) {
                        response.setContent(new Content().addMediaType("application/json", new MediaType()));
                    }
                }
            });
        }
        wrapSuccessResponseSchemas(responses);

        return operation;
    }

    private void wrapSuccessResponseSchemas(ApiResponses responses) {
        responses.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("2") || entry.getKey().equals("default"))
                .forEach(entry -> {
                    ApiResponse response = entry.getValue();

                    // 기존 response 스키마를 커스텀 공통 응답 포맷으로 래핑
                    wrapSuccessResponseSchema(response);
                });
    }

    private void wrapSuccessResponseSchema(ApiResponse response) {
        if (response.getContent() != null) {
            response.getContent().forEach((mediaTypeKey, mediaType) -> {
                Schema<?> originalSchema = mediaType.getSchema();

                // 새로운 응답 schema 만들기
                Schema<?> newSchema = createResponseSchema(
                        originalSchema == null ? new ObjectSchema().nullable(true) : originalSchema);
                newSchema.getProperties().get("status").setExample(SUCCESS_STATUS_MESSAGE);

                // 기존 mediaType에 새로운 schema 세팅
                mediaType.setSchema(newSchema);
            });
        }
    }

    // 실제 공통 응답의 타입과 필수 키를 명세에 반영
    static Schema<?> createResponseSchema(Schema<?> dataSchema) {
        Schema<?> schema = new ObjectSchema().nullable(false);
        schema.addProperty("status", new StringSchema().nullable(false));
        schema.addProperty("data", dataSchema);
        schema.addProperty("timestamp", new StringSchema().format("date-time").nullable(false)
                .example("2025-08-08T14:10:32.123"));
        schema.setRequired(List.of("status", "data", "timestamp"));
        return schema;
    }
}
