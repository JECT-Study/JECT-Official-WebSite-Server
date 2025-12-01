package org.ject.support.common.webclient;

import io.netty.channel.ChannelOption;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    private static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 3_000; // 연결 최대 시간
    private static final Duration DEFAULT_RESPONSE_TIMEOUT = Duration.ofSeconds(5); // 응답 대기 최대 시간

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(DEFAULT_RESPONSE_TIMEOUT)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, DEFAULT_CONNECT_TIMEOUT_MILLIS);

        return builder
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
