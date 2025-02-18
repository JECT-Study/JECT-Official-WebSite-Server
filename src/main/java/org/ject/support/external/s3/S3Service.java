package org.ject.support.external.s3;

import lombok.RequiredArgsConstructor;
import org.ject.support.common.util.PeriodAccessible;
import org.ject.support.domain.file.dto.CreatePresignedUrlResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private static final int EXPIRE_MINUTES = 10;

    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucket;

    /**
     * 사용자가 첨부한 파일 이름과 해당 사용자의 식별자를 토대로 Pre-signed URL 생성
     */
    @PeriodAccessible
    public CreatePresignedUrlResponse createPresignedUrl(Long memberId, String fileName) {
        String keyName = getKeyName(memberId, fileName);
        PutObjectPresignRequest presignRequest = getPutObjectPresignRequest(keyName);
        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        return getCreatePresignedUrlResponse(keyName, presignedRequest);
    }

    /**
     * 여러 개의 Pre-signed URL 생성
     */
    @PeriodAccessible
    public List<CreatePresignedUrlResponse> createPresignedUrls(Long memberId, List<String> fileNames) {
        return fileNames.stream()
                .map(fileName -> {
                    String keyName = getKeyName(memberId, fileName);
                    PutObjectPresignRequest presignRequest = getPutObjectPresignRequest(keyName);
                    PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
                    return getCreatePresignedUrlResponse(keyName, presignedRequest);
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

    private CreatePresignedUrlResponse getCreatePresignedUrlResponse(String keyName, PresignedPutObjectRequest presignedRequest) {
        return new CreatePresignedUrlResponse(
                keyName,
                presignedRequest.url().toExternalForm(),
                LocalDateTime.ofInstant(presignedRequest.expiration(), ZoneId.systemDefault()));
    }
}
