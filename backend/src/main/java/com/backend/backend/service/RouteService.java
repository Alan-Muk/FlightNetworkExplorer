package com.backend.backend.service;

import com.backend.backend.client.GraphServiceClient;
import com.backend.backend.dto.GraphPathsResponse;
import com.backend.backend.dto.RouteComparisonResponse;
import com.backend.backend.dto.RouteDetailsResponse;
import com.backend.backend.dto.RouteOption;
import com.backend.backend.exception.AirportNotFoundException;
import com.backend.backend.model.Airport;
import com.backend.backend.model.Route;
import com.backend.backend.repository.AirportRepository;
import com.backend.backend.repository.RouteRepository;
import com.backend.backend.util.GeoUtils;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for retrieving and analysing flight route information.
 *
 * <p>Provides details about direct routes, compares alternative routes between airports, computes
 * distances and estimated flight times, and ranks route options by distance and stop count.
 */
@Service
public class RouteService {

  private final AirportRepository airportRepository;
  private final RouteRepository routeRepository;
  private final GraphServiceClient graphServiceClient;

  public RouteService(
      AirportRepository airportRepository,
      RouteRepository routeRepository,
      GraphServiceClient graphServiceClient) {
    this.airportRepository = airportRepository;
    this.routeRepository = routeRepository;
    this.graphServiceClient = graphServiceClient;
  }

  @Transactional(readOnly = true)
  public RouteDetailsResponse getRoute(String from, String to) {
    String fromCode = from == null ? "" : from.trim().toUpperCase();
    String toCode = to == null ? "" : to.trim().toUpperCase();

    Airport origin =
        airportRepository
            .findByIata(fromCode)
            .orElseThrow(() -> new AirportNotFoundException(fromCode));

    Airport destination =
        airportRepository
            .findByIata(toCode)
            .orElseThrow(() -> new AirportNotFoundException(toCode));

    List<Route> routes = routeRepository.findBySourceIataAndDestinationIata(fromCode, toCode);

    double distance =
        GeoUtils.haversine(
            origin.getLatitude(),
            origin.getLongitude(),
            destination.getLatitude(),
            destination.getLongitude());

    RouteDetailsResponse response = new RouteDetailsResponse();
    response.setFrom(fromCode);
    response.setTo(toCode);
    response.setDistanceKm(Math.round(distance));
    response.setEstimatedFlightTime(estimateTime(distance));
    response.setDirect(!routes.isEmpty());
    response.setAirlines(
        routes.stream()
            .map(Route::getAirline)
            .filter(Objects::nonNull)
            .distinct()
            .sorted()
            .toList());
    return response;
  }

  @Transactional(readOnly = true)
  public RouteComparisonResponse compareRoutes(String from, String to) {
    String fromCode = from == null ? "" : from.trim().toUpperCase();
    String toCode = to == null ? "" : to.trim().toUpperCase();

    GraphPathsResponse pathResponse = graphServiceClient.paths(fromCode, toCode);
    List<List<String>> paths = pathResponse.paths();

    RouteComparisonResponse response = new RouteComparisonResponse();

    if (paths == null || paths.isEmpty()) {
      response.setRoutes(List.of());
      return response;
    }

    List<RouteOption> options =
        paths.stream()
            .map(path -> buildOptionFromAirports(fromCode, toCode, path))
            .collect(Collectors.toList());

    rankRoutes(options);
    response.setRoutes(options);
    return response;
  }

  /**
   * Builds a route option from a sequence of airport IATA codes.
   *
   * <p>Each leg's distance is counted once — multiple airlines flying the same physical leg share
   * the same distance, so counting every route row would over-count.
   */
  private RouteOption buildOptionFromAirports(String from, String to, List<String> airports) {
    RouteOption option = new RouteOption();

    double distance = 0;
    List<String> airlines = new ArrayList<>();

    for (int i = 0; i < airports.size() - 1; i++) {
      String source = airports.get(i);
      String destination = airports.get(i + 1);

      List<Route> routes = routeRepository.findBySourceIataAndDestinationIata(source, destination);

      // Count the leg distance once, not once per airline row.
      if (!routes.isEmpty()) {
        Double legDistance = routes.get(0).getDistanceKm();
        if (legDistance != null) {
          distance += legDistance;
        }
      }

      for (Route route : routes) {
        airlines.addAll(route.getAirlines());
      }
    }

    option.setId(String.join("-", airports));
    option.setFrom(from);
    option.setTo(to);
    option.setAirports(airports);
    option.setStops(Math.max(0, airports.size() - 2));
    option.setDistanceKm(distance);
    option.setEstimatedFlightTime(estimateTime(distance));
    option.setAirlines(airlines.stream().distinct().sorted().toList());
    option.setColour("#ffffff");
    return option;
  }

  private void rankRoutes(List<RouteOption> routes) {
    if (routes.isEmpty()) return;

    RouteOption shortest =
        routes.stream().min(Comparator.comparingDouble(RouteOption::getDistanceKm)).orElse(null);
    if (shortest != null) {
      shortest.setShortest(true);
      shortest.setColour("#00ff88");
    }

    RouteOption longest =
        routes.stream().max(Comparator.comparingDouble(RouteOption::getDistanceKm)).orElse(null);
    if (longest != null) {
      longest.setColour("#ff4444");
    }

    RouteOption fastest =
        routes.stream()
            .min(Comparator.comparingInt(r -> parseMinutes(r.getEstimatedFlightTime())))
            .orElse(null);

    RouteOption fewestStops =
        routes.stream().min(Comparator.comparingInt(RouteOption::getStops)).orElse(null);
    if (fewestStops != null) {
      fewestStops.setLeastConnected(true);
    }

    RouteOption mostStops =
        routes.stream().max(Comparator.comparingInt(RouteOption::getStops)).orElse(null);
    if (mostStops != null) {
      mostStops.setMostConnected(true);
    }
  }

  /**
   * Parses a duration string like "8h", "8h 30m", or "1d 4h" into total minutes. Returns {@code
   * Integer.MAX_VALUE} on parse failure so it sorts to the end.
   */
  private static int parseMinutes(String estimatedFlightTime) {
    if (estimatedFlightTime == null || estimatedFlightTime.isBlank()) {
      return Integer.MAX_VALUE;
    }
    java.util.regex.Matcher matcher =
        java.util.regex.Pattern.compile("(\\d+)([dhm])").matcher(estimatedFlightTime);
    int totalMinutes = 0;
    boolean matched = false;
    while (matcher.find()) {
      matched = true;
      int value = Integer.parseInt(matcher.group(1));
      switch (matcher.group(2)) {
        case "d" -> totalMinutes += value * 24 * 60;
        case "h" -> totalMinutes += value * 60;
        case "m" -> totalMinutes += value;
        default -> {}
      }
    }
    return matched ? totalMinutes : Integer.MAX_VALUE;
  }

  /**
   * Rough flight time estimate: cruise time plus a fixed ground overhead for taxi, takeoff, and
   * approach. Cruise speed ~850 km/h, overhead ~30 minutes.
   *
   * <p>Formatted as {@code "8h"}, {@code "8h 30m"}, or {@code "1d 4h"} depending on the duration.
   */
  private static String estimateTime(double distanceKm) {
    if (distanceKm <= 0) {
      return "0m";
    }

    double cruiseHours = distanceKm / 850.0;
    double totalHours = cruiseHours + 0.5; // 30 minutes ground overhead
    long totalMinutes = Math.round(totalHours * 60);

    long days = totalMinutes / (24 * 60);
    long hours = (totalMinutes % (24 * 60)) / 60;
    long minutes = totalMinutes % 60;

    if (days > 0) {
      return hours > 0 ? days + "d " + hours + "h" : days + "d";
    }
    if (hours > 0) {
      return minutes > 0 ? hours + "h " + minutes + "m" : hours + "h";
    }
    return minutes + "m";
  }
}
