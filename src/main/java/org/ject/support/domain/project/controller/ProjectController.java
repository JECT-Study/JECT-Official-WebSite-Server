package org.ject.support.domain.project.controller;

import lombok.RequiredArgsConstructor;
import org.ject.support.domain.project.dto.ProjectDetailResponse;
import org.ject.support.domain.project.dto.ProjectResponse;
import org.ject.support.domain.project.service.ProjectService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
    public Page<ProjectResponse> findProjects(@RequestParam String semester,
                                              @PageableDefault(size = 20) Pageable pageable) {
        return projectService.findProjectsBySemester(semester, pageable);
    }

    @GetMapping("/{projectId}")
    public ProjectDetailResponse findProjectDetails(@PathVariable Long projectId) {
        return projectService.findProjectDetails(projectId);
    }
}
