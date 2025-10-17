package io.services.monitor.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

  @Bean
  public WebClient kgWebClient(
      @Value("${monitor.api.url}") String baseUrl,
      @Value("${monitor.api.timeout-ms}") int timeoutMs) {

    HttpClient httpClient = HttpClient.create()
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeoutMs)
        .responseTimeout(Duration.ofMillis(timeoutMs))
        .doOnConnected(conn -> conn
            .addHandlerLast(new ReadTimeoutHandler(timeoutMs / 1000))
            .addHandlerLast(new WriteTimeoutHandler(timeoutMs / 1000))
        );

    return WebClient.builder()
        .baseUrl(baseUrl)
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .exchangeStrategies(ExchangeStrategies.builder()
            .codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(256 * 1024))
            .build())
        .build();
  }
}