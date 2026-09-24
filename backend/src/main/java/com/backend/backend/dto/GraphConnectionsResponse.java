package com.backend.backend.dto;

import java.util.List;

/**
 * Response from the graph service's {@code GET /connections/{airport}} endpoint.
 *
 * <p>Example: {@code {"airport": "AMS", "connections": ["LHR", "CDG", "JFK"]}}.
 */
public record GraphConnectionsResponse(String airport, List<String> connections) {
  public GraphConnectionsResponse {
    if (connections == null) {
      connections = List.of();
    }
  }
}
