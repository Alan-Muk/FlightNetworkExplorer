package com.backend.backend.client;

import com.backend.backend.config.GraphServiceProperties;
import com.backend.backend.dto.GraphCentralityResponse;
import com.backend.backend.dto.GraphConnectionsResponse;
import com.backend.backend.dto.GraphPathResponse;
import com.backend.backend.dto.GraphPathsResponse;
import com.backend.backend.exception.GraphServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/**
 * HTTP client for the Python graph service.
 *
 * <p>Wraps a {@link RestClient} configured with explicit connect and read timeouts (see {@link
 * com.backend.backend.config.GraphServiceConfig}). All methods normalise IATA codes to uppercase.
 *
 * <p>404 responses from the graph service are treated as "no result" and returned as an empty
 * response rather than propagated as errors. Connection failures and 5xx responses from the graph
 * service are rethrown as {@link GraphServiceUnavailableException} so the API returns 503 rather
 * than a generic 500.
 */
@Component
public class GraphServiceClient {

  private static final Logger log = LoggerFactory.getLogger(GraphServiceClient.class);

  private final RestClient client;
  private final GraphServiceProperties props;

  public GraphServiceClient(RestClient graphServiceRestClient, GraphServiceProperties props) {
    this.client = graphServiceRestClient;
    this.props = props;
  }

  public GraphConnectionsResponse connections(String airport) {
    String code = normalise(airport);
    log.debug("GET /connections/{}", code);
    try {
      return client
          .get()
          .uri("/connections/{airport}", code)
          .retrieve()
          .body(GraphConnectionsResponse.class);
    } catch (ResourceAccessException | HttpServerErrorException e) {
      throw unavailable(e);
    }
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
    } catch (ResourceAccessException | HttpServerErrorException e) {
      throw unavailable(e);
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
    } catch (ResourceAccessException | HttpServerErrorException e) {
      throw unavailable(e);
    }
  }

  public GraphCentralityResponse centrality(String metric, int limit) {
    String normalisedMetric = metric == null ? "degree" : metric.trim().toLowerCase();
    log.debug("GET /centrality?metric={}&limit={}", normalisedMetric, limit);
    try {
      return client
          .get()
          .uri(
              uriBuilder ->
                  uriBuilder
                      .path("/centrality")
                      .queryParam("metric", normalisedMetric)
                      .queryParam("limit", limit)
                      .build())
          .retrieve()
          .body(GraphCentralityResponse.class);
    } catch (HttpClientErrorException e) {
      log.warn("Graph service error for /centrality: {}", e.getStatusCode());
      throw e;
    } catch (ResourceAccessException | HttpServerErrorException e) {
      throw unavailable(e);
    }
  }

  private GraphServiceUnavailableException unavailable(Exception cause) {
    log.warn("Graph service at {} is unavailable: {}", props.url(), cause.getMessage());
    return new GraphServiceUnavailableException(props.url(), cause);
  }

  private static String normalise(String code) {
    if (code == null) {
      throw new IllegalArgumentException("Airport code is required");
    }
    return code.trim().toUpperCase();
  }
}
