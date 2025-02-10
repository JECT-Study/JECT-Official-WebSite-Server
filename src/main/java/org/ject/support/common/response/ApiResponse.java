package org.ject.support.common.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private final String status;
    private final T data;
    private final LocalDateTime timestamp = LocalDateTime.now();
}
