package org.ject.support.admin.semester.dto;

import org.ject.support.domain.recruit.domain.SemesterEvent;

public record SemesterEventResponse(
        Long id,
        String name
) {

    public static SemesterEventResponse from(SemesterEvent semesterEvent) {
        return new SemesterEventResponse(semesterEvent.getId(), semesterEvent.getName());
    }
}
