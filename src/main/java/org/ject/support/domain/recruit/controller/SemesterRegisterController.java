package org.ject.support.domain.recruit.controller;

import lombok.RequiredArgsConstructor;
import org.ject.support.domain.recruit.dto.SemesterRegisterRequest;
import org.ject.support.domain.recruit.service.SemesterRegisterUsecase;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/semesters")
public class SemesterRegisterController implements SemesterRegisterApiSpec {

    private final SemesterRegisterUsecase semesterRegisterUsecase;

    @Override
    @PostMapping
    public void register(@RequestBody SemesterRegisterRequest request) {
        semesterRegisterUsecase.registerSemester(request);
    }
}
