package org.ject.support.common.springdoc;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.media.Schema;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.ject.support.common.security.AuthPrincipal;
import org.springdoc.core.models.GroupedOpenApi;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.core.jackson.ModelResolver;

@Configuration
@RequiredArgsConstructor
public class SpringdocConfig {

    static {
        // 모든 enum을 공통 Schema로 등록
        ModelResolver.enumsAsRef = true;

        // 인증 정보에서 주입하는 파라미터를 API 명세에서 제외
        SpringDocUtils.getConfig().addAnnotationsToIgnore(AuthPrincipal.class);
    }

    private final SuccessResponseCustomizer successResponseCustomizer;
    private final ErrorResponseCustomizer errorResponseCustomizer;

    @Bean
    public GlobalOpenApiCustomizer pageResponseSchemaCustomizer() {
        // PageImpl 기반 응답과 공통 페이지 응답에서 항상 반환하는 메타정보 명시
        Set<String> commonProperties = Set.of("content", "number", "size", "totalElements",
                "totalPages", "last", "numberOfElements");
        return openApi -> {
            if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
                return;
            }
            openApi.getComponents().getSchemas().forEach((name, schema) -> {
                Schema<?> pageSchema = schema;
                if (name.startsWith("Page") && pageSchema.getProperties() != null
                        && pageSchema.getProperties().containsKey("content")) {
                    List<String> requiredProperties = pageSchema.getProperties().keySet().stream()
                            .filter(commonProperties::contains).toList();
                    requiredProperties.forEach(property -> pageSchema.getProperties().get(property).setNullable(false));
                    requiredProperties.forEach(pageSchema::addRequiredItem);
                }
            });
        };
    }

    @Bean
    public OpenAPI openAPI() {
        String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                // API 기본 정보
                .info(new Info()
                        .title("JECT Makers API Document")
                        .version("v0.0.1")
                        .description("JECT 메이커스 팀 API 명세서입니다."))

                // Security 스키마 정의
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .description("JWT 토큰을 입력하세요 (Bearer prefix 제외)")))
                // 서버 정보
                .addServersItem(new Server().url("/"))
                // 전역으로 Security 적용
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName));
    }

    @Bean
    public GroupedOpenApi coreApi() {
        return GroupedOpenApi.builder()
                .group("Core API")
                .addOperationCustomizer(successResponseCustomizer)
                .addOperationCustomizer(errorResponseCustomizer)
                .pathsToExclude(
                        "/admin/**",
                        "/admin-auth/**"
                )
                .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("Admin API")
                .addOperationCustomizer(successResponseCustomizer)
                .addOperationCustomizer(errorResponseCustomizer)
                .pathsToMatch(
                        "/admin/**",
                        "/admin-auth/**"
                )
                .build();
    }
}
