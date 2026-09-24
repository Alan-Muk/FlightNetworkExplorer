package com.backend.backend.client;

import com.backend.backend.dto.GraphConnectionsResponse;
import com.backend.backend.dto.GraphPathResponse;
import com.backend.backend.dto.GraphPathsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

/**
 * HTTP client for the Python graph service.
 *
 * <p>Wraps a {@link RestClient} configured with explicit connect and read timeouts (see {@link
 * com.backend.backend.config.GraphServiceConfig}). All methods normalise IATA codes to uppercase.
 * 404 responses from the graph service are treated as "no result" and returned as an empty response
 * rather than propagated as errors.
 */
@Component
public class GraphServiceClient {

  private static final Logger log = LoggerFactory.getLogger(GraphServiceClient.class);

  private final RestClient client;

  public GraphServiceClient(RestClient graphServiceRestClient) {
    this.client = graphServiceRestClient;
  }

  public GraphConnectionsResponse connections(String airport) {
    String code = normalise(airport);
    log.debug("GET /connections/{}", code);
    return client
        .get()
        .uri("/connections/{airport}", code)
        .retrieve()
        .body(GraphConnectionsResponse.class);
  }

  public GraphPathResponse path(String source, String destination) {
    String from = normalise(source);
    String to = normalise(destination);
    log.debug("GET /path/{}/{}", from, to);
    try {
      return client
          .get()
          .uri("/path/{from}/{to}", from, to)
          .retrieve()
          .body(GraphPathResponse.class);
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        log.debug(
            "No path from {} to {} — graph service said: {}",
            from,
            to,
            e.getResponseBodyAsString());
        return GraphPathResponse.empty(from, to);
      }
      log.warn("Graph service error for /path/{}/{}: {}", from, to, e.getStatusCode());
      throw e;
    }
  }

  public GraphPathsResponse paths(String source, String destination) {
    String from = normalise(source);
    String to = normalise(destination);
    log.debug("GET /paths/{}/{}", from, to);
    try {
      return client
          .get()
          .uri("/paths/{from}/{to}", from, to)
          .retrieve()
          .body(GraphPathsResponse.class);
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        log.debug(
            "No paths from {} to {} — graph service said: {}",
            from,
            to,
            e.getResponseBodyAsString());
        return GraphPathsResponse.empty(from, to);
      }
      log.warn("Graph service error for /paths/{}/{}: {}", from, to, e.getStatusCode());
      throw e;
    }
  }

  private static String normalise(String code) {
    if (code == null) {
      throw new IllegalArgumentException("Airport code is required");
    }
    return code.trim().toUpperCase();
  }
}
