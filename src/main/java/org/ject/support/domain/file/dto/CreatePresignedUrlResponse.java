package org.ject.support.domain.file.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CreatePresignedUrlResponse(String keyName, String presignedUrl, LocalDateTime expiration) {
}
