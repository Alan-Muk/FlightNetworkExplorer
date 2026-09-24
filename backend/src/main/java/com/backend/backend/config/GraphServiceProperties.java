package com.backend.backend.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the external Python graph service.
 *
 * <p>Bound from properties prefixed with {@code graph.service.*}. Timeouts default to 2s connect
 * and 5s read — enough for local calls, short enough to fail fast when the service is down.
 */
@ConfigurationProperties(prefix = "graph.service")
public record GraphServiceProperties(String url, Duration connectTimeout, Duration readTimeout) {
  public GraphServiceProperties {
    if (url == null || url.isBlank()) {
      throw new IllegalArgumentException("graph.service.url must be set");
    }
    if (connectTimeout == null) {
      connectTimeout = Duration.ofSeconds(2);
    }
    if (readTimeout == null) {
      readTimeout = Duration.ofSeconds(5);
    }
  }
}
