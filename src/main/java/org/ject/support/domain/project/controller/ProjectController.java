package org.ject.support.domain.project.controller;

import lombok.RequiredArgsConstructor;
import org.ject.support.domain.project.dto.ProjectDetailResponse;
import org.ject.support.domain.project.dto.ProjectResponse;
import org.ject.support.domain.project.entity.Project;
import org.ject.support.domain.project.service.ProjectService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public Page<ProjectResponse> findProjects(@RequestParam(defaultValue = "MAIN") final Project.Category category,
                                              @RequestParam final String semester,
                                              final Pageable pageable) {
        return projectService.findProjects(category, semester, pageable);
    }

    @GetMapping("/{projectId}")
    public ProjectDetailResponse findProjectDetails(@PathVariable final Long projectId) {
        return projectService.findProjectDetails(projectId);
    }
}
