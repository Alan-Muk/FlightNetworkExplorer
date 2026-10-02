package com.backend.backend.service;

import com.backend.backend.exception.RouteImportException;
import com.backend.backend.model.Airport;
import com.backend.backend.model.Route;
import com.backend.backend.repository.AirportRepository;
import com.backend.backend.repository.RouteRepository;
import com.backend.backend.util.GeoUtils;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Imports route data from the OpenFlights {@code routes.dat} file into the database on application
 * startup.
 *
 * <p>Runs after {@link AirportImportService} (via {@code @Order}) so that airport coordinates are
 * available for computing each route's distance using the Haversine formula.
 */
@Component
@Order(3)
public class RouteImportService implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(RouteImportService.class);

  private static final int COL_AIRLINE = 0;
  private static final int COL_SOURCE_IATA = 2;
  private static final int COL_DESTINATION_IATA = 4;
  private static final int MIN_COLUMNS = 9;
  private static final int PROGRESS_INTERVAL = 10_000;

  private final RouteRepository repository;
  private final AirportRepository airportRepository;
  private final Path routesFile;

  public RouteImportService(
      RouteRepository repository,
      AirportRepository airportRepository,
      @Value("${routes.file:../data/raw/routes.dat}") String routesFile) {
    this.repository = repository;
    this.airportRepository = airportRepository;
    this.routesFile = Path.of(routesFile).toAbsolutePath().normalize();
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    long existing = repository.count();
    if (existing > 0) {
      log.info("Routes already loaded ({} rows) — skipping import", existing);
      return;
    }

    if (!Files.exists(routesFile)) {
      throw new RouteImportException("Routes file not found: " + routesFile);
    }

    log.info("Importing routes from {}", routesFile);

    List<Route> routes = new ArrayList<>();
    int parsed = 0;
    int skipped = 0;

    try (Reader reader = Files.newBufferedReader(routesFile, StandardCharsets.UTF_8);
        CSVParser parser = CSVFormat.DEFAULT.parse(reader)) {

      for (CSVRecord record : parser) {
        if (record.size() < MIN_COLUMNS) {
          skipped++;
          continue;
        }

        try {
          String sourceIata = clean(record.get(COL_SOURCE_IATA));
          String destinationIata = clean(record.get(COL_DESTINATION_IATA));

          if (isMissing(sourceIata) || isMissing(destinationIata)) {
            skipped++;
            continue;
          }

          Route route = new Route();
          route.setAirline(clean(record.get(COL_AIRLINE)));
          route.setSourceIata(sourceIata);
          route.setDestinationIata(destinationIata);
          routes.add(route);

          parsed++;
          if (parsed % PROGRESS_INTERVAL == 0) {
            log.info("Parsed {} routes so far (line {})", parsed, parser.getCurrentLineNumber());
          }

        } catch (Exception e) {
          skipped++;
          log.warn(
              "Skipping malformed row at line {}: {}",
              parser.getCurrentLineNumber(),
              e.getMessage());
        }
      }
    } catch (IOException e) {
      throw new RouteImportException("Failed to read routes file: " + routesFile, e);
    }

    if (routes.isEmpty()) {
      log.warn("No valid routes found in {} — nothing imported", routesFile);
      return;
    }

    int withDistance = computeDistances(routes);

    repository.saveAll(routes);
    log.info(
        "Route import completed: {} imported, {} skipped, {} with distance",
        parsed,
        skipped,
        withDistance);
  }

  /**
   * Computes Haversine distance for each route using airport coordinates loaded from the database.
   * Routes with missing coordinates are left with a {@code null} distance.
   *
   * @return the number of routes that received a non-null distance
   */
  private int computeDistances(List<Route> routes) {
    Map<String, Airport> lookup = new HashMap<>();
    for (Airport airport : airportRepository.findAll()) {
      if (airport.getIata() != null) {
        lookup.put(airport.getIata().toUpperCase(), airport);
      }
    }

    int withDistance = 0;
    for (Route route : routes) {
      Airport from = lookup.get(route.getSourceIata());
      Airport to = lookup.get(route.getDestinationIata());
      if (from == null || to == null) {
        continue;
      }
      double distance =
          GeoUtils.haversine(
              from.getLatitude(), from.getLongitude(), to.getLatitude(), to.getLongitude());
      route.setDistanceKm(distance);
      withDistance++;
    }
    return withDistance;
  }

  private static boolean isMissing(String value) {
    return value.isBlank() || value.equals("\\N");
  }

  private static String clean(String value) {
    return value == null ? "" : value.replace("\"", "").trim().toUpperCase();
  }
}
