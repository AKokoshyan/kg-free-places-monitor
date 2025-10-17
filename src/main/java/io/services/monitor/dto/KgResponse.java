package io.services.monitor.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KgResponse(
    @JsonProperty("free-places") FreePlaces freePlaces
) {
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static record FreePlaces(
      @JsonProperty("KLAS_DATE") String klasDate,
      @JsonProperty("SPR_SWOBODNI_MESTA") String freeCount,
      @JsonProperty("IS_FINAL") String isFinal
  ) { }
}