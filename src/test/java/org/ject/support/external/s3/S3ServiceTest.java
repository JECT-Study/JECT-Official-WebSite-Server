package org.ject.support.external.s3;

import org.ject.support.domain.file.dto.CreatePresignedUrlResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.MalformedURLException;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private S3Presigner s3Presigner;

    @InjectMocks
    private S3Service s3Service;

    private TestParameter testParameter;

    @BeforeEach
    void setUp() throws MalformedURLException {
        testParameter = new TestParameter(123L, List.of("test1.pdf", "test2.pdf"), Instant.now().plusSeconds(600));
        PresignedPutObjectRequest mockRequest = mock(PresignedPutObjectRequest.class);
        when(mockRequest.url()).thenReturn(URI.create(testParameter.expectedUrls.get(0)).toURL());
        when(mockRequest.expiration()).thenReturn(testParameter.expirationTime);
        when(s3Presigner.presignPutObject((PutObjectPresignRequest) any())).thenReturn(mockRequest);
    }

    @Test
    @DisplayName("create pre-signed url")
    void create_presigned_url() {
        // when
        List<CreatePresignedUrlResponse> result =
                s3Service.createPresignedUrl(testParameter.memberId, testParameter.fileNames);

        // then
        assertThat(result).hasSize(2);

        CreatePresignedUrlResponse firstResponse = result.get(0);
        assertThat(firstResponse.keyName()).contains(testParameter.fileNames.get(0));
        assertThat(firstResponse.presignedUrl()).isEqualTo(testParameter.expectedUrls.get(0));
        assertThat(firstResponse.expiration())
                .isEqualTo(LocalDateTime.ofInstant(testParameter.expirationTime, ZoneId.systemDefault()));

        CreatePresignedUrlResponse secondResponse = result.get(1);
        assertThat(secondResponse.keyName()).contains(testParameter.fileNames.get(1));
    }

    @Test
    @DisplayName("create key name")
    void create_key_name() {
        // when
        List<CreatePresignedUrlResponse> result =
                s3Service.createPresignedUrl(testParameter.memberId, testParameter.fileNames);

        // then
        assertThat(result).hasSize(2);

        CreatePresignedUrlResponse firstResponse = result.get(0);
        assertThat(firstResponse.keyName()).contains(testParameter.memberId.toString());
        assertThat(removePrefix(firstResponse.keyName())).startsWith(testParameter.fileNames.get(0));
        assertThat(firstResponse.keyName()).contains("_");

        CreatePresignedUrlResponse secondResponse = result.get(1);
        assertThat(secondResponse.keyName()).contains(testParameter.memberId.toString());
        assertThat(removePrefix(secondResponse.keyName())).startsWith(testParameter.fileNames.get(1));
        assertThat(secondResponse.keyName()).contains("_");
    }

    private String removePrefix(String keyName) {
        String prefix = String.format("%s/", testParameter.memberId);
        return keyName.replace(prefix, "");
    }

    static class TestParameter {
        Long memberId;
        List<String> fileNames;
        Instant expirationTime;
        List<String> expectedKeyNames;
        List<String> expectedUrls;

        public TestParameter(Long memberId, List<String> fileNames, Instant expirationTime) {
            this.memberId = memberId;
            this.fileNames = fileNames;
            this.expectedKeyNames = fileNames.stream().map(fileName -> getExpectedKeyName(memberId, fileName)).toList();
            this.expectedUrls = expectedKeyNames.stream().map(this::getExpectedUrls).toList();
            this.expirationTime = expirationTime;
        }

        private String getExpectedKeyName(Long memberId, String fileName) {
            return String.format("%s/%s.pdf_uuid", memberId, fileName);
        }

        private String getExpectedUrls(String keyName) {
            return String.format("%s%s", "https://s3.test.com/", keyName);
        }
    }
}