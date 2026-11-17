package org.ject.support.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
    private final String status;
    @Schema(nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
    private final T data;
    @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
    private final LocalDateTime timestamp=LocalDateTime.now();
}
