package org.ject.support.admin.semester.controller;

import lombok.RequiredArgsConstructor;
import org.ject.support.admin.semester.dto.SemesterEventsResponse;
import org.ject.support.admin.semester.service.AdminSemesterEventService;
import org.ject.support.domain.recruit.domain.SemesterEventType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/semesters/{semesterId}/events")
public class AdminSemesterEventController implements AdminSemesterEventApiSpec {

    private final AdminSemesterEventService adminSemesterEventService;

    // 선택한 기수의 행사 목록 조회
    @Override
    @GetMapping
    public SemesterEventsResponse getEvents(
            @PathVariable Long semesterId,
            @RequestParam SemesterEventType type
    ) {
        return adminSemesterEventService.getEvents(semesterId, type);
    }

}
