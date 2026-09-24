package com.backend.backend.service;

import com.backend.backend.exception.RouteImportException;
import com.backend.backend.model.Route;
import com.backend.backend.repository.RouteRepository;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Imports route data from the OpenFlights {@code routes.dat} file into the database on application
 * startup.
 *
 * <p>The import runs once, guarded by a row-count check. If the {@code routes} table already
 * contains data, the import is skipped. The import runs inside a transaction, so a failure
 * mid-import rolls back cleanly and the next startup will retry.
 *
 * <p>Column layout of {@code routes.dat} (0-indexed):
 *
 * <pre>
 *   0: airline IATA          (e.g. "2B")
 *   1: airline ID            (unused)
 *   2: source airport IATA   (e.g. "AER")
 *   3: source airport ID     (unused)
 *   4: destination airport IATA
 *   5: destination airport ID (unused)
 *   6: codeshare             (unused)
 *   7: stops                 (unused)
 *   8: equipment             (unused)
 * </pre>
 */
@Component
public class RouteImportService implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(RouteImportService.class);

  private static final int COL_AIRLINE = 0;
  private static final int COL_SOURCE_IATA = 2;
  private static final int COL_DESTINATION_IATA = 4;
  private static final int MIN_COLUMNS = 9;
  private static final int PROGRESS_INTERVAL = 10_000;

  private final RouteRepository repository;
  private final Path routesFile;

  public RouteImportService(
      RouteRepository repository,
      @Value("${routes.file:../data/raw/routes.dat}") String routesFile) {
    this.repository = repository;
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

    repository.saveAll(routes);
    log.info("Route import completed: {} imported, {} skipped", parsed, skipped);
  }

  private static boolean isMissing(String value) {
    return value.isBlank() || value.equals("\\N");
  }

  private static String clean(String value) {
    return value == null ? "" : value.replace("\"", "").trim().toUpperCase();
  }
}
