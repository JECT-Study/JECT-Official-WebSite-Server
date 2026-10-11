package org.ject.support.external.email.config;

import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;

@Configuration
@DependsOn(value = {"awsConfig"})
public class SesConfig {

    @Bean
    public SesV2Client sesV2Client(AwsCredentials awsCredentials, Region region) {
        return SesV2Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .region(region)
                // 발송 결과가 불확실한 호출의 SDK 내부 자동 재발송을 차단한다.
                .overrideConfiguration(configuration -> configuration.retryPolicy(RetryPolicy.none())
                        .apiCallTimeout(Duration.ofSeconds(30))
                        .apiCallAttemptTimeout(Duration.ofSeconds(20)))
                .build();
    }
}
