package org.ject.support.domain.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.ject.support.common.springdoc.CustomApiResponse;
import org.ject.support.domain.project.dto.ProjectDetailResponse;
import org.ject.support.domain.project.dto.ProjectResponse;
import org.ject.support.domain.project.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Project", description = "프로젝트 API")
public interface ProjectApi {

    @Operation(
            summary = "프로젝트 목록 조회",
            description = "카테고리와 기수 ID를 통해 프로젝트 목록을 조회합니다.")
    @CustomApiResponse
    Page<ProjectResponse> findProjects(@RequestParam final Project.Category category,
                                       @RequestParam(required = false) final Long semesterId,
                                       final Pageable pageable);

    @Operation(
            summary = "프로젝트 상세 조회",
            description = "전달한 ID에 해당하는 프로젝트의 상세 내용을 조회합니다.")
    @CustomApiResponse
    ProjectDetailResponse findProjectDetails(@PathVariable final Long projectId);
}
