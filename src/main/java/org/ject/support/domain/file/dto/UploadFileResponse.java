package org.ject.support.domain.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record UploadFileResponse(
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        String cdnUrl,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        String presignedUrl,
        @Schema(nullable = false, requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime expiration
) {
}
