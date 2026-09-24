package com.backend.backend.controller;

import com.backend.backend.client.GraphServiceClient;
import com.backend.backend.dto.GraphConnectionsResponse;
import com.backend.backend.dto.GraphPathResponse;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Thin pass-through to the Python graph service.
 *
 * <p>Endpoints here mirror the graph service API. Unlike the rest of the backend, they return the
 * graph service's response shape directly.
 */
@RestController
@RequestMapping(value = "/api/graph", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class GraphController {

  private final GraphServiceClient graph;

  public GraphController(GraphServiceClient graph) {
    this.graph = graph;
  }

  @GetMapping("/connections/{airport}")
  public GraphConnectionsResponse connections(
      @PathVariable @Pattern(regexp = "^[A-Za-z]{3}$", message = "IATA code must be 3 letters")
          String airport) {
    return graph.connections(airport);
  }

  @GetMapping("/path/{from}/{to}")
  public GraphPathResponse path(
      @PathVariable @Pattern(regexp = "^[A-Za-z]{3}$", message = "IATA code must be 3 letters")
          String from,
      @PathVariable @Pattern(regexp = "^[A-Za-z]{3}$", message = "IATA code must be 3 letters")
          String to) {
    return graph.path(from, to);
  }
}
