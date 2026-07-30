package org.ject.support.admin.semester.dto;

import java.util.List;
import org.ject.support.domain.recruit.domain.SemesterEvent;
import org.ject.support.domain.recruit.domain.SemesterEventType;

public record SemesterEventsResponse(
        SemesterEventType type,
        List<SemesterEventResponse> events
) {

    public static SemesterEventsResponse from(
            SemesterEventType type,
            List<SemesterEvent> semesterEvents
    ) {
        List<SemesterEventResponse> events = semesterEvents.stream()
                .map(SemesterEventResponse::from)
                .toList();
        return new SemesterEventsResponse(type, events);
    }
}
