package org.ject.support.domain.project.controller;

import lombok.RequiredArgsConstructor;
import org.ject.support.domain.project.dto.ProjectDetailResponse;
import org.ject.support.domain.project.dto.ProjectResponse;
import org.ject.support.domain.project.service.ProjectService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public Page<ProjectResponse> findProjects(@RequestParam String semester, Pageable pageable) {
        return projectService.findProjectsBySemester(semester, pageable);
    }

    @GetMapping("/{projectId}")
    public ProjectDetailResponse findProjects(@PathVariable Long projectId) {
        return projectService.findProjectDetails(projectId);
    }
}
