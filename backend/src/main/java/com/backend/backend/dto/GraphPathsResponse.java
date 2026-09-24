package com.backend.backend.dto;

import java.util.List;

/**
 * Response from the graph service's {@code GET /paths/{source}/{destination}} endpoint.
 *
 * <p>Example: {@code {"from": "AMS", "to": "JFK", "paths": [["AMS","LHR","JFK"],
 * ["AMS","CDG","JFK"]]}}.
 *
 * <p>A 404 from the graph service is normalised to an empty path list.
 */
public record GraphPathsResponse(String from, String to, List<List<String>> paths) {
  public GraphPathsResponse {
    if (paths == null) {
      paths = List.of();
    }
  }

  public static GraphPathsResponse empty(String from, String to) {
    return new GraphPathsResponse(from, to, List.of());
  }
}
