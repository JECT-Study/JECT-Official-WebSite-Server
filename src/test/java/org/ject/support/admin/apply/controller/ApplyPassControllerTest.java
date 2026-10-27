package org.ject.support.admin.apply.controller;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.ject.support.admin.apply.dto.ApplyPassRequest;
import org.ject.support.admin.apply.service.ApplyPassService;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.common.exception.GlobalExceptionHandler;
import org.ject.support.common.response.ObjectMapperConfig;
import org.ject.support.common.response.ResponseWrapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ApplyPassControllerTest extends UnitTestSupport {

    @InjectMocks
    private ApplyPassController controller;
    @Mock
    private ApplyPassService service;

    @Test
    void 합격_처리는_건수_없이_공통_성공_응답을_반환한다() throws Exception {
        // given
        var mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler(), new ResponseWrapper())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(new ObjectMapperConfig().objectMapper()))
                .build();

        // when, then
        mockMvc.perform(post("/admin/apply/pass").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"applyIds\":[1,2]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data").doesNotExist());
        verify(service).passApply(new ApplyPassRequest(List.of(1L, 2L)));
    }
}
