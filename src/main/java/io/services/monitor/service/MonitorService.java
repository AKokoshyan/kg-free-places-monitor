package io.services.monitor.service;

import io.services.monitor.dto.KgResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class MonitorService {
  private static final Logger log = LoggerFactory.getLogger(MonitorService.class);

  private final KgApiClient apiClient;
  private final EmailNotifier emailNotifier;

  public MonitorService(KgApiClient apiClient, EmailNotifier emailNotifier) {
    this.apiClient = apiClient;
    this.emailNotifier = emailNotifier;
  }

  /**
   * Cron configured in application.yml: every 5 minutes
   */
  @Scheduled(cron = "${monitor.schedule.cron}")
  public void poll() {
    log.debug("Polling KG free places...");
    try {
      KgResponse response = apiClient.checkFreePlaces().onErrorResume(err -> {
        log.warn("KG API call failed: {}", err.toString());
        return Mono.empty();
      }).block();

      if (response == null) {
        return; // already logged
      }

      boolean isNoFreeSpaces = isNoFreeSpaces(response);

      if (isNoFreeSpaces) {

        log.info("No free spaces.");
        return;
      }

      emailNotifier.notifyChange(response);

    } catch (Exception e) {
      log.error("Unexpected error in poll()", e);
    }
  }

  /**
   * Business rule from the prompt: if SPR_SWOBODNI_MESTA == null no free spaces.
   */
  static boolean isNoFreeSpaces(KgResponse response) {
    KgResponse.FreePlaces fp = response.freePlaces();
    if (fp == null) {
      return true; // be conservative: treat missing as no slots (avoids spam)
    }
    String free = fp.freeCount();
    return free == null;
  }
}