package org.ject.support.external.email.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.ject.support.base.UnitTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.ApiCallAttemptTimeoutException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;
import software.amazon.awssdk.services.sesv2.model.SesV2Exception;

class SesConfigTest extends UnitTestSupport {

    @Test
    void SES_호출과_단일_시도에_시간_제한을_적용한다() {
        // given
        var credentials = AwsBasicCredentials.create("test-access-key", "test-secret-key");

        // when
        try (SesV2Client client = new SesConfig().sesV2Client(credentials, Region.US_EAST_1)) {
            var configuration = client.serviceClientConfiguration().overrideConfiguration();

            // then
            assertThat(configuration.apiCallTimeout()).contains(Duration.ofSeconds(30));
            assertThat(configuration.apiCallAttemptTimeout()).contains(Duration.ofSeconds(20));
        }
    }

    @Test
    @Timeout(10)
    void 응답이_없는_SES_호출은_시간_초과로_끝나고_자동_재발송하지_않는다() throws Exception {
        // given
        AtomicInteger attempts = new AtomicInteger();
        CountDownLatch release = new CountDownLatch(1);
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            attempts.incrementAndGet();
            exchange.getRequestBody().close();
            try {
                release.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            } finally {
                exchange.close();
            }
        });
        server.start();
        var credentials = AwsBasicCredentials.create("test-access-key", "test-secret-key");
        try (SesV2Client configured = new SesConfig().sesV2Client(credentials, Region.US_EAST_1);
             SesV2Client client = SesV2Client.builder()
                     .credentialsProvider(StaticCredentialsProvider.create(credentials))
                     .region(Region.US_EAST_1)
                     .endpointOverride(URI.create("http://127.0.0.1:" + server.getAddress().getPort()))
                     // 실제 SDK 취소 동작은 짧은 한도로 검증하고 운영 기본값은 별도 테스트에서 확인한다.
                     .overrideConfiguration(configured.serviceClientConfiguration().overrideConfiguration()
                             .toBuilder().apiCallTimeout(Duration.ofSeconds(3))
                             .apiCallAttemptTimeout(Duration.ofSeconds(1)).build())
                     .build()) {
            // when, then
            assertThatThrownBy(() -> client.sendEmail(SendEmailRequest.builder().build()))
                    .isInstanceOf(ApiCallAttemptTimeoutException.class);
            assertThat(attempts.get()).isEqualTo(1);
        } finally {
            release.countDown();
            server.stop(0);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {500, 429})
    void 서버_오류와_호출_제한은_SDK가_자동_재발송하지_않는다(int statusCode) throws Exception {
        // given
        AtomicInteger attempts = new AtomicInteger();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            attempts.incrementAndGet();
            exchange.getRequestBody().close();
            byte[] body = "{\"message\":\"uncertain outcome\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.getResponseHeaders().set("x-amzn-ErrorType",
                    statusCode == 429 ? "TooManyRequestsException" : "InternalServiceErrorException");
            exchange.sendResponseHeaders(statusCode, body.length);
            try (var response = exchange.getResponseBody()) {
                response.write(body);
            }
        });
        server.start();
        var credentials = AwsBasicCredentials.create("test-access-key", "test-secret-key");
        try (SesV2Client configuredClient = new SesConfig().sesV2Client(credentials, Region.US_EAST_1);
             SesV2Client client = SesV2Client.builder()
                     .credentialsProvider(StaticCredentialsProvider.create(credentials))
                     .region(Region.US_EAST_1)
                     .endpointOverride(URI.create("http://127.0.0.1:" + server.getAddress().getPort()))
                     .overrideConfiguration(configuredClient.serviceClientConfiguration().overrideConfiguration())
                     .build()) {
            // when, then
            assertThatThrownBy(() -> client.sendEmail(SendEmailRequest.builder().build()))
                    .isInstanceOf(SesV2Exception.class);
            assertThat(attempts.get()).isEqualTo(1);
        } finally {
            server.stop(0);
        }
    }
}
