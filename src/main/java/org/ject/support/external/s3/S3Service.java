package org.ject.support.external.s3;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.ject.support.common.util.PeriodAccessible;
import org.ject.support.domain.file.dto.UploadFileResponse;
import org.ject.support.domain.file.exception.FileErrorCode;
import org.ject.support.domain.file.exception.FileException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class S3Service {

    private static final int EXPIRE_MINUTES = 10;

    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucket;

    /**
     * 지원자가 첨부한 포트폴리오 파일 이름과 해당 지원자의 식별자를 토대로 Pre-signed URL 생성
     */
    @PeriodAccessible
    public List<UploadFileResponse> uploadPortfolios(Long memberId, List<String> fileNames) {
        validatePortfolioExtension(fileNames);
        return createPresignedUrls(memberId, fileNames);
    }

    /**
     * USER 이상의 권한을 가진 사용자가 첨부한 파일 이름과 해당 사용자의 식별자를 토대로 Pre-signed URL 생성
     */
    public List<UploadFileResponse> uploadContents(Long memberId, List<String> fileNames) {
        return createPresignedUrls(memberId, fileNames);
    }

    private void validatePortfolioExtension(List<String> fileNames) {
        if (fileNames.stream().anyMatch(fileName -> !fileName.endsWith("pdf"))) {
            throw new FileException(FileErrorCode.INVALID_EXTENSION);
        }
    }

    private List<UploadFileResponse> createPresignedUrls(Long memberId, List<String> fileNames) {
        return fileNames.stream()
                .map(fileName -> {
                    String keyName = getKeyName(memberId, fileName);
                    PutObjectPresignRequest presignRequest = getPutObjectPresignRequest(keyName);
                    PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
                    return getUploadFileResponse(keyName, presignedRequest);
                })
                .toList();
    }

    private String getKeyName(Long memberId, String fileName) {
        String uniqueFileName = String.format("%s_%s", fileName, UUID.randomUUID());
        return String.format("%s/%s", memberId, uniqueFileName);
    }

    private PutObjectPresignRequest getPutObjectPresignRequest(String keyName) {
        return PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(EXPIRE_MINUTES))
                .putObjectRequest(getPutObjectRequest(keyName))
                .build();
    }

    private PutObjectRequest getPutObjectRequest(String keyName) {
        return PutObjectRequest.builder()
                .bucket(bucket)
                .key(keyName)
                .build();
    }

    private UploadFileResponse getUploadFileResponse(String keyName, PresignedPutObjectRequest presignedRequest) {
        return UploadFileResponse.builder()
                .keyName(keyName)
                .presignedUrl(presignedRequest.url().toExternalForm())
                .expiration(LocalDateTime.ofInstant(presignedRequest.expiration(), ZoneId.systemDefault()))
                .build();
    }
}
