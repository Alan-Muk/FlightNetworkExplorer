package com.backend.backend.service;

import com.backend.backend.dto.AirportStatsResponse;
import com.backend.backend.exception.AirportNotFoundException;
import com.backend.backend.model.Airport;
import com.backend.backend.model.Route;
import com.backend.backend.repository.AirportRepository;
import com.backend.backend.repository.RouteRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Computes statistics for a single airport from the stored route data.
 *
 * <p>For the requested IATA code, produces:
 *
 * <ul>
 *   <li>{@code connections} — distinct destinations + distinct origins
 *   <li>{@code outgoingRoutes} — distinct destinations reachable from this airport
 *   <li>{@code incomingRoutes} — distinct origins that reach this airport
 *   <li>{@code topDestinations} — up to 10 destinations, ordered by how many routes serve them
 *   <li>{@code airlines} — up to 10 airlines, ordered alphabetically
 * </ul>
 *
 * <p>Counts are distinct-per-airport rather than row-based, so a physical route flown by multiple
 * airlines counts once. Self-loops (routes where source equals destination) are excluded from
 * {@code topDestinations}.
 */
@Service
public class AirportStatsService {

  private static final int TOP_LIMIT = 10;

  private final AirportRepository airportRepository;
  private final RouteRepository routeRepository;

  public AirportStatsService(AirportRepository airportRepository, RouteRepository routeRepository) {
    this.airportRepository = airportRepository;
    this.routeRepository = routeRepository;
  }

  @Transactional(readOnly = true)
  public AirportStatsResponse getStats(String iata) {
    String code = iata == null ? "" : iata.trim().toUpperCase();

    Airport airport =
        airportRepository.findByIata(code).orElseThrow(() -> new AirportNotFoundException(code));

    List<Route> outgoing = routeRepository.findBySourceIata(code);
    List<Route> incoming = routeRepository.findByDestinationIata(code);

    // --- distinct destinations / origins ---
    List<String> distinctDestinations =
        outgoing.stream()
            .map(Route::getDestinationIata)
            .filter(Objects::nonNull)
            .filter(dest -> !dest.equalsIgnoreCase(code)) // exclude self-loops
            .distinct()
            .toList();

    List<String> distinctOrigins =
        incoming.stream()
            .map(Route::getSourceIata)
            .filter(Objects::nonNull)
            .filter(origin -> !origin.equalsIgnoreCase(code))
            .distinct()
            .toList();

    // --- top destinations by frequency ---
    Map<String, Long> destinationCounts =
        outgoing.stream()
            .map(Route::getDestinationIata)
            .filter(Objects::nonNull)
            .filter(dest -> !dest.equalsIgnoreCase(code))
            .collect(Collectors.groupingBy(dest -> dest, Collectors.counting()));

    List<String> topDestinations =
        destinationCounts.entrySet().stream()
            .sorted(
                Map.Entry.<String, Long>comparingByValue()
                    .reversed()
                    .thenComparing(Map.Entry.comparingByKey()))
            .limit(TOP_LIMIT)
            .map(Map.Entry::getKey)
            .toList();

    // --- airlines (alphabetical) ---
    List<String> airlines =
        java.util.stream.Stream.concat(outgoing.stream(), incoming.stream())
            .map(Route::getAirline)
            .filter(Objects::nonNull)
            .filter(s -> !s.isBlank())
            .distinct()
            .sorted(Comparator.naturalOrder())
            .limit(TOP_LIMIT)
            .toList();

    AirportStatsResponse response = new AirportStatsResponse();
    response.setIata(airport.getIata());
    response.setName(airport.getName());
    response.setConnections(distinctDestinations.size() + distinctOrigins.size());
    response.setOutgoingRoutes(distinctDestinations.size());
    response.setIncomingRoutes(distinctOrigins.size());
    response.setTopDestinations(topDestinations);
    response.setAirlines(airlines);
    return response;
  }
}
