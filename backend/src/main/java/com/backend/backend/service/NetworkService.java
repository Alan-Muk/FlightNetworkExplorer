package com.backend.backend.service;

import com.backend.backend.dto.NetworkResponse;
import com.backend.backend.exception.AirportNotFoundException;
import com.backend.backend.model.Airport;
import com.backend.backend.model.Route;
import com.backend.backend.repository.AirportRepository;
import com.backend.backend.repository.RouteRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Builds the direct connection network around a given airport, for map visualisation.
 *
 * <p>Returns the source airport plus every destination directly reachable from it, together with
 * the edges connecting them. Edges are deduplicated per (from, to) pair so multiple airlines flying
 * the same physical route render as a single arc.
 */
@Service
public class NetworkService {

  private static final Logger log = LoggerFactory.getLogger(NetworkService.class);

  private final AirportRepository airportRepository;
  private final RouteRepository routeRepository;

  public NetworkService(AirportRepository airportRepository, RouteRepository routeRepository) {
    this.airportRepository = airportRepository;
    this.routeRepository = routeRepository;
  }

  @Transactional(readOnly = true)
  public NetworkResponse getNetwork(String iata) {
    if (iata == null || iata.isBlank()) {
      throw new IllegalArgumentException("IATA code is required");
    }
    String airportIata = iata.trim().toUpperCase();

    Airport source =
        airportRepository
            .findByIata(airportIata)
            .orElseThrow(() -> new AirportNotFoundException(airportIata));

    List<Route> routes = routeRepository.findBySourceIata(airportIata);

    // Deterministic node order (LinkedHashMap + sort by IATA)
    Map<String, NetworkResponse.AirportNode> nodes = new LinkedHashMap<>();
    nodes.put(source.getIata(), toNode(source));

    List<String> destinationIatas =
        routes.stream().map(Route::getDestinationIata).distinct().toList();

    if (!destinationIatas.isEmpty()) {
      airportRepository.findByIataIn(destinationIatas).stream()
          .sorted(Comparator.comparing(Airport::getIata))
          .forEach(destination -> nodes.put(destination.getIata(), toNode(destination)));
    }

    // Deduplicate edges by (from, to) — multiple airlines flying the same route
    // should produce a single arc on the map.
    List<NetworkResponse.RouteEdge> edges =
        routes.stream()
            .map(Route::getDestinationIata)
            .filter(nodes::containsKey)
            .distinct()
            .map(destination -> newEdge(airportIata, destination))
            .toList();

    long dropped = routes.stream().map(Route::getDestinationIata).distinct().count() - edges.size();
    if (dropped > 0) {
      log.warn(
          "Dropped {} route(s) from {} referencing unknown destination airports",
          dropped,
          airportIata);
    }

    NetworkResponse response = new NetworkResponse();
    response.setAirport(airportIata);
    response.setNodes(new ArrayList<>(nodes.values()));
    response.setEdges(edges);
    return response;
  }

  private NetworkResponse.AirportNode toNode(Airport airport) {
    NetworkResponse.AirportNode node = new NetworkResponse.AirportNode();
    node.setIata(airport.getIata());
    node.setName(airport.getName());
    node.setLatitude(airport.getLatitude());
    node.setLongitude(airport.getLongitude());
    return node;
  }

  private NetworkResponse.RouteEdge newEdge(String from, String to) {
    NetworkResponse.RouteEdge edge = new NetworkResponse.RouteEdge();
    edge.setFrom(from);
    edge.setTo(to);
    return edge;
  }
}
