package org.ject.support.common.springdoc;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import static org.ject.support.common.response.ApiResponseConstant.SUCCESS_STATUS_MESSAGE;

@Component
public class SuccessResponseCustomizer implements OperationCustomizer {

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        ApiResponses responses = operation.getResponses();
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
                Schema<Object> newSchema = new Schema<>();
                newSchema.addProperty("status", new Schema<>().example(SUCCESS_STATUS_MESSAGE));
                newSchema.addProperty("data", originalSchema);
                newSchema.addProperty("timestamp", new Schema<>().example("2025-08-08T14:10:32.123"));

                // 기존 mediaType에 새로운 schema 세팅
                mediaType.setSchema(newSchema);
            });
        }
    }
}
