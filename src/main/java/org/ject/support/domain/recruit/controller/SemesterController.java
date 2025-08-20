package org.ject.support.domain.recruit.controller;

import lombok.RequiredArgsConstructor;
import org.ject.support.domain.recruit.dto.SemesterRegisterRequest;
import org.ject.support.domain.recruit.dto.SemesterResponses;
import org.ject.support.domain.recruit.service.SemesterInquiryUsecase;
import org.ject.support.domain.recruit.service.SemesterRegisterUsecase;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/semesters")
public class SemesterController implements SemesterApiSpec {
    private final SemesterRegisterUsecase semesterRegisterUsecase;
    private final SemesterInquiryUsecase semesterInquiryUsecase;

    @Override
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void register(@RequestBody SemesterRegisterRequest request) {
        semesterRegisterUsecase.registerSemester(request);
    }

    /**
     * 모든 기수 목록을 조회합니다.
     *
     * @return 기수 목록
     */
    @Override
    @GetMapping
    public SemesterResponses getAllSemesters() {
        return semesterInquiryUsecase.getAllSemesters();
    }
}
