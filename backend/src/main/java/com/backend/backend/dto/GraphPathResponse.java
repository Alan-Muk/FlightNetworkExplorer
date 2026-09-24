package com.backend.backend.dto;

import java.util.List;

/**
 * Response from the graph service's {@code GET /path/{source}/{destination}} endpoint.
 *
 * <p>Example: {@code {"from": "AMS", "to": "JFK", "path": ["AMS", "LHR", "JFK"]}}.
 *
 * <p>A 404 from the graph service is normalised to an empty path (see {@link
 * com.backend.backend.client.GraphServiceClient}).
 */
public record GraphPathResponse(String from, String to, List<String> path) {
  public GraphPathResponse {
    if (path == null) {
      path = List.of();
    }
  }

  public static GraphPathResponse empty(String from, String to) {
    return new GraphPathResponse(from, to, List.of());
  }
}
