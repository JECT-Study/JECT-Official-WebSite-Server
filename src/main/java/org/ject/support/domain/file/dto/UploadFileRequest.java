package org.ject.support.domain.file.dto;

public record UploadFileRequest(String name, String contentType, long contentLength) {
}
