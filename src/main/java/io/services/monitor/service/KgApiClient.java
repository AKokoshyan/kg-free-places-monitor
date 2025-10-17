package io.services.monitor.service;


import io.services.monitor.dto.KgRequest;
import io.services.monitor.dto.KgResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class KgApiClient {
  private static final Logger log = LoggerFactory.getLogger(KgApiClient.class);

  private final WebClient webClient;
  private final String reception;
  private final int maxAttempts;
  private final long backoffMs;

  public KgApiClient(WebClient kgWebClient,
      @Value("${monitor.api.reception}") String reception,
      @Value("${monitor.api.retry.max-attempts}") int maxAttempts,
      @Value("${monitor.api.retry.backoff-ms}") long backoffMs) {
    this.webClient = kgWebClient;
    this.reception = reception;
    this.maxAttempts = maxAttempts;
    this.backoffMs = backoffMs;
  }

  public Mono<KgResponse> checkFreePlaces() {
    KgRequest body = new KgRequest(reception);
    return webClient.post()
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(body)
        .retrieve()
        .bodyToMono(KgResponse.class)
        .doOnNext(resp -> log.info("KG API responded: {}", resp))
        .retryWhen(Retry.backoff(Math.max(1, maxAttempts - 1), Duration.ofMillis(backoffMs))
            .onRetryExhaustedThrow((spec, signal) -> signal.failure()));
  }
}
