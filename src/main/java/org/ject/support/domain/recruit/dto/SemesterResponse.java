package org.ject.support.domain.recruit.dto;

import org.ject.support.domain.recruit.domain.Semester;

public record SemesterResponse(Long id, String name) {
    public static SemesterResponse from(Semester semester) {
        return new SemesterResponse(
                semester.getId(), 
                semester.getName()
        );
    }
}
