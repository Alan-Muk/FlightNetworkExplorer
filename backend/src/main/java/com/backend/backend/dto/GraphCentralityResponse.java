package com.backend.backend.dto;

import java.util.List;

/**
 * Response from the graph service's {@code GET /centrality} endpoint.
 *
 * <p>Example:
 *
 * <pre>
 * {
 *   "metric": "degree",
 *   "results": [
 *     {"iata": "FRA", "score": 0.4234},
 *     {"iata": "CDG", "score": 0.4102}
 *   ]
 * }
 * </pre>
 */
public record GraphCentralityResponse(String metric, List<AirportScore> results) {

  public GraphCentralityResponse {
    if (results == null) {
      results = List.of();
    }
  }

  public record AirportScore(String iata, double score) {}
}
