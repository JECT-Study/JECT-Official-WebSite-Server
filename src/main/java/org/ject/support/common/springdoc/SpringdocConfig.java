package org.ject.support.common.springdoc;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

@Configuration
public class SpringdocConfig {

    private static final String EX_SUCCESS_STATUS = "SUCCESS";
    private static final String EX_TIMESTAMP = "2025-08-08T14:10:32.123";

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("JECT Makers API Document")
                .version("v0.0.1")
                .description("JECT 메이커스 팀 API 명세서입니다.");

        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("Bearer Token", securityScheme))
                .addServersItem(new Server().url("/"))
                .info(info);
    }

    @Bean
    public OperationCustomizer customize() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            CustomApiResponse methodAnnotation = handlerMethod.getMethodAnnotation(CustomApiResponse.class);

            ApiResponses responses = operation.getResponses();
            responses.entrySet().stream()
                    .filter(entry -> entry.getKey().startsWith("2")) // 상태 코드 2xx 필터링
                    .forEach(entry -> {
                        ApiResponse response = entry.getValue();

                        for (Header header : methodAnnotation.headers()) {
                            response.addHeaderObject(header.name(),
                                    new io.swagger.v3.oas.models.headers.Header()
                                            .description(header.description())
                                            .schema(new Schema<>().type("string")));
                        }

                        if (response.getContent() != null) {
                            response.getContent().forEach((mediaTypeKey, mediaType) -> {
                                Schema<?> originalSchema = mediaType.getSchema();

                                // 새로운 응답 스키마 만들기
                                Schema<Object> wrapperSchema = new Schema<>();
                                wrapperSchema.addProperties("status", new Schema<>().example(EX_SUCCESS_STATUS));
                                wrapperSchema.addProperties("data", originalSchema);
                                wrapperSchema.addProperties("timestamp", new Schema<>().example(EX_TIMESTAMP));

                                // 기존 mediaType에 새로운 schema 세팅
                                mediaType.setSchema(wrapperSchema);
                            });
                        }
                    });

            return operation;
        };
    }
}
