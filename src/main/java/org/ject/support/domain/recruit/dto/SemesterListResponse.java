package org.ject.support.domain.recruit.dto;

import org.ject.support.domain.recruit.domain.Semester;

public record SemesterListResponse(
        Long id,
        String name
) {
    public static SemesterListResponse from(Semester semester) {
        return new SemesterListResponse(
                semester.getId(), 
                semester.getName()
        );
    }
}
