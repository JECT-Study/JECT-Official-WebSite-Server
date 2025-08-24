//package org.ject.support.common.springdoc;
//
//import io.swagger.v3.oas.models.Operation;
//import io.swagger.v3.oas.models.media.Content;
//import io.swagger.v3.oas.models.media.MediaType;
//import io.swagger.v3.oas.models.media.ObjectSchema;
//import io.swagger.v3.oas.models.media.Schema;
//import io.swagger.v3.oas.models.media.StringSchema;
//import io.swagger.v3.oas.models.responses.ApiResponse;
//import io.swagger.v3.oas.models.responses.ApiResponses;
//import org.junit.jupiter.api.Test;
//
//import java.util.Map;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.ject.support.common.response.ApiResponseConstant.SUCCESS_STATUS_MESSAGE;
//
//class SuccessResponseCustomizerTest {
//
//    SuccessResponseCustomizer customizer = new SuccessResponseCustomizer();
//
//    @Test
//    void 응답이_2xx이면_공통스키마로_래핑한다() {
//        Operation op = new Operation();
//        ApiResponses responses = new ApiResponses();
//        op.setResponses(responses);
//
//        ApiResponse ok = new ApiResponse();
//        Content okContent = new Content();
//        MediaType okJson = new MediaType();
//        Schema<?> original200 = new StringSchema();
//        okJson.setSchema(original200);
//        okContent.addMediaType("application/json", okJson);
//        ok.setContent(okContent);
//        responses.addApiResponse("200", ok);
//
//        // when
//        customizer.customize(op, null);
//
//        // then
//        Schema<?> wrapped200 = op.getResponses()
//                .get("200").getContent().get("application/json").getSchema();
//        Map<String, Schema> properties = wrapped200.getProperties();
//        assertThat(properties).isNotNull();
//        assertThat(original200).isEqualTo(properties.get("data"));
//        assertThat(SUCCESS_STATUS_MESSAGE).isEqualTo(properties.get("status").getExample());
//        assertThat("2025-08-08T14:10:32.123").isEqualTo(properties.get("timestamp").getExample());
//    }
//
//    @Test
//    void 응답이_2xx가_아니면_래핑하지_않는다() {
//        // given
//        Operation op = new Operation();
//        ApiResponses responses = new ApiResponses();
//        op.setResponses(responses);
//
//        ApiResponse bad = new ApiResponse();
//        Content badContent = new Content();
//        MediaType badJson = new MediaType();
//        Schema<?> original400 = new ObjectSchema();
//        badJson.setSchema(original400);
//        badContent.addMediaType("application/json", badJson);
//        bad.setContent(badContent);
//        responses.addApiResponse("400", bad);
//
//        // when
//        customizer.customize(op, null);
//
//        // then
//        Schema<?> after = op.getResponses().get("400")
//                .getContent().get("application/json").getSchema();
//        assertThat(original400).isEqualTo(after);
//    }
//}
