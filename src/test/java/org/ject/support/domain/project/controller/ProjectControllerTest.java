package org.ject.support.domain.project.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.ject.support.testconfig.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
@AutoConfigureMockMvc
@Transactional
class ProjectControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("inquire projects")
    void inquireProjects() throws Exception {
        mockMvc.perform(get("/projects")
//                        .param("category", "MAIN")
                        .param("semester", "2023-1"))
                .andExpect(status().isOk())
                .andExpectAll(
                        content().string(containsString("hasNext")),
                        content().string(containsString("items")),
                        content().string(containsString("totalElements")),
                        content().string(containsString("totalPages")),
                        content().string(containsString("pageNumber")),
                        content().string(containsString("pageSize"))
                )
        ;
    }

}
