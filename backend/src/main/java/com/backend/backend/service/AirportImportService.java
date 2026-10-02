package com.backend.backend.service;

import com.backend.backend.exception.AirportImportException;
import com.backend.backend.model.Airport;
import com.backend.backend.repository.AirportRepository;
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
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(1)
public class AirportImportService implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(AirportImportService.class);

  private static final int COL_ID = 0;
  private static final int COL_NAME = 1;
  private static final int COL_CITY = 2;
  private static final int COL_COUNTRY = 3;
  private static final int COL_IATA = 4;
  private static final int COL_ICAO = 5;
  private static final int COL_LATITUDE = 6;
  private static final int COL_LONGITUDE = 7;
  private static final int MIN_COLUMNS = 8;
  private static final String NULL_MARKER = "\\N";

  private final AirportRepository repository;
  private final Path airportsFile;

  public AirportImportService(
      AirportRepository repository,
      @Value("${airports.file:../data/raw/airports.dat}") String airportsFile) {
    this.repository = repository;
    this.airportsFile = Path.of(airportsFile).toAbsolutePath().normalize();
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    long existing = repository.count();
    if (existing > 0) {
      log.info("Airports already loaded ({} rows) — skipping import", existing);
      return;
    }

    if (!Files.exists(airportsFile)) {
      throw new AirportImportException("Airports file not found: " + airportsFile);
    }

    log.info("Importing airports from {}", airportsFile);

    List<Airport> airports = new ArrayList<>();
    int parsed = 0;
    int skipped = 0;

    try (Reader reader = Files.newBufferedReader(airportsFile, StandardCharsets.UTF_8);
        CSVParser parser = CSVFormat.DEFAULT.parse(reader)) {

      for (CSVRecord record : parser) {
        if (record.size() < MIN_COLUMNS) {
          skipped++;
          continue;
        }

        try {
          Airport airport = new Airport();
          airport.setId(Long.parseLong(cleanRaw(record.get(COL_ID))));
          airport.setName(cleanRaw(record.get(COL_NAME)));
          airport.setCity(cleanOrNull(record.get(COL_CITY)));
          airport.setCountry(cleanOrNull(record.get(COL_COUNTRY)));
          airport.setIata(cleanCode(record.get(COL_IATA), 3));
          airport.setIcao(cleanCode(record.get(COL_ICAO), 4));
          airport.setLatitude(parseDoubleOrZero(record.get(COL_LATITUDE)));
          airport.setLongitude(parseDoubleOrZero(record.get(COL_LONGITUDE)));
          airports.add(airport);
          parsed++;

        } catch (Exception e) {
          skipped++;
          log.warn(
              "Skipping malformed airport at line {}: {}",
              parser.getCurrentLineNumber(),
              e.getMessage());
        }
      }
    } catch (IOException e) {
      throw new AirportImportException("Failed to read airports file: " + airportsFile, e);
    }

    if (airports.isEmpty()) {
      log.warn("No valid airports found in {} — nothing imported", airportsFile);
      return;
    }

    repository.saveAll(airports);
    log.info("Airport import completed: {} imported, {} skipped", parsed, skipped);
  }

  private static String cleanRaw(String value) {
    return value == null ? null : value.replace("\"", "").trim();
  }

  private static String cleanOrNull(String value) {
    String cleaned = cleanRaw(value);
    if (cleaned == null || cleaned.isEmpty() || NULL_MARKER.equals(cleaned)) {
      return null;
    }
    return cleaned;
  }

  private static String cleanCode(String value, int len) {
    String cleaned = cleanRaw(value);
    if (cleaned == null || cleaned.isEmpty()) return null;
    cleaned = cleaned.toUpperCase();
    if (cleaned.length() != len) return null;
    if (!cleaned.matches("[A-Z]+")) return null;
    return cleaned;
  }

  private static double parseDoubleOrZero(String value) {
    String cleaned = cleanOrNull(value);
    return cleaned == null ? 0.0 : Double.parseDouble(cleaned);
  }
}
