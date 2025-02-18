package org.ject.support.domain.project.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.domain.project.dto.ProjectDetailResponse;
import org.ject.support.domain.project.dto.ProjectResponse;
import org.ject.support.domain.project.exception.ProjectErrorCode;
import org.ject.support.domain.project.exception.ProjectException;
import org.ject.support.domain.project.repository.ProjectQueryRepositoryImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectQueryRepositoryImpl projectQueryRepository;

    /**
     * 주어진 기수의 프로젝트를 모두 조회합니다.
     */
    public Page<ProjectResponse> findProjectsBySemester(String semester, Pageable pageable) {
        return projectQueryRepository.findProjectsBySemester(semester, pageable);
    }

    /**
     * 프로젝트 상세 정보를 조회합니다.
     */
    public ProjectDetailResponse findProjectDetails(Long projectId) {
        return projectQueryRepository.findProjectDetails(projectId)
                .orElseThrow(() -> new ProjectException(ProjectErrorCode.NOT_FOUND));
    }
}
