package org.ject.support.external.email.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import org.ject.support.base.UnitTestSupport;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;
import software.amazon.awssdk.services.sesv2.model.SesV2Exception;

class SesConfigTest extends UnitTestSupport {

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
